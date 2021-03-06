package com.ngsaihor.medialearning.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.ngsaihor.medialearning.databinding.ActivityCamera2PreviewBinding
import com.ngsaihor.medialearning.mdeia.video.VideoEncoderManager
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque


class Camera2PreviewActivity : AppCompatActivity() {

    val TAG = "xxx"

    private lateinit var binding: ActivityCamera2PreviewBinding

    private lateinit var cameraManager: CameraManager
    private lateinit var imageReader: ImageReader
    private lateinit var previewImageReader: ImageReader
    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null

    private var cameraId: String = ""

    private var previewSurfaceTexture: SurfaceTexture? = null
    private var previewSurface: Surface? = null
    private var jpegSurface: Surface? = null

    private lateinit var cameraCharacteristics: CameraCharacteristics

    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null
    private var supportFormat: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamera2PreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.capture.setOnClickListener {
            createImageReader()
        }
        binding.recording.setOnClickListener {
            if (!VideoEncoderManager.isRunning) {
                this@Camera2PreviewActivity.lifecycleScope.launch {
                    VideoEncoderManager.startEncode()
                }
            }else{
                VideoEncoderManager.isRunning = false
            }
        }
        startBackgroundThread()
        initCamera()
    }

    private fun startBackgroundThread() {
        bgThread = HandlerThread("CameraBackground").also { it.start() }
        bgHandler = bgThread?.looper?.let { Handler(it) }
    }

    private fun stopBackgroundThread() {
        bgThread?.quitSafely()
        try {
            bgThread?.join()
            bgThread = null
            bgHandler = null
        } catch (e: InterruptedException) {

        }
    }

    private inner class PreviewSurfaceTextureListener : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {

        }


        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

        }


        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {

            return true
        }


        override fun onSurfaceTextureAvailable(
            surfaceTexture: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            previewSurfaceTexture = surfaceTexture
            val previewSize =
                getOptimalSize(cameraCharacteristics, SurfaceTexture::class.java, width, height)
            previewSize?.let {
                VideoEncoderManager.initEncoder(
                    Environment.getExternalStorageDirectory().absolutePath, previewSize.width,
                    previewSize.height
                )
                previewSurfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
                previewSurface = Surface(surfaceTexture)
                previewImageReader = ImageReader.newInstance(
                    previewSize.width,
                    previewSize.height,
                    ImageFormat.YUV_420_888,
                    5
                )
                previewImageReader.setOnImageAvailableListener(
                    OnPreviewImageAvailableListener(),
                    null
                )
                createCameraCaptureSession()
            }

        }
    }

    private fun initCamera() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIdList = cameraManager.cameraIdList
        if (cameraIdList.isEmpty()) {
            return
        }
        cameraIdList.forEach {
            val cameraCharacteristics = cameraManager.getCameraCharacteristics(it)
            val facing = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                cameraId = it
                this@Camera2PreviewActivity.cameraCharacteristics = cameraCharacteristics
                return@forEach
            }
        }
        binding.textureView.surfaceTextureListener = PreviewSurfaceTextureListener()
        openCamera()
    }


    private fun getOptimalSize(
        cameraCharacteristics: CameraCharacteristics,
        clazz: Class<*>,
        maxWidth: Int,
        maxHeight: Int
    ): Size? {
        val aspectRatio = maxWidth.toFloat() / maxHeight
        val streamConfigurationMap =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = streamConfigurationMap?.getOutputSizes(clazz)
        streamConfigurationMap?.outputFormats?.forEach {
            supportFormat = it
        }
        if (supportedSizes != null) {
            for (size in supportedSizes) {
                if (size.width.toFloat() / size.height == aspectRatio && size.height <= maxHeight && size.width <= maxWidth) {
                    return size
                }
            }
        }
        return null
    }

    private val cameraStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            Log.d(TAG, "cameraDevice onOpened:${cameraDevice.id}")
            this@Camera2PreviewActivity.cameraDevice = cameraDevice
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {
            Log.d(TAG, "cameraDevice Disconnected:${cameraDevice.id}")
        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {
            Log.d(TAG, "cameraDevice id:${cameraDevice.id} error:$error")
        }

    }

    private fun openCamera() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        if (cameraId.isBlank()) {
            return
        }
        cameraManager.openCamera(cameraId, cameraStateCallback, bgHandler)
    }

    private inner class SessionStateCallback : CameraCaptureSession.StateCallback() {

        override fun onConfigureFailed(session: CameraCaptureSession) {
            Log.d(TAG, "onConfigureFailed")
        }


        override fun onConfigured(session: CameraCaptureSession) {
            Log.d(TAG, "onConfigured success")
            this@Camera2PreviewActivity.session = session
            createPreviewRequest(session)
        }


        override fun onClosed(session: CameraCaptureSession) {
            Log.d(TAG, "session onClosed")
        }
    }

    private fun createCameraCaptureSession() {
        val imageSize = getOptimalSize(
            cameraCharacteristics,
            SurfaceTexture::class.java,
            binding.textureView.measuredWidth,
            binding.textureView.measuredHeight
        )
        imageSize?.let {
            imageReader = ImageReader.newInstance(
                imageSize.width,
                imageSize.height,
                ImageFormat.YUV_420_888,
                5
            )
            imageReader.setOnImageAvailableListener(OnJpegImageAvailableListener(), bgHandler)
            jpegSurface = imageReader.surface
        }
        val sessionStateCallback = SessionStateCallback()
        val outputs = listOf(previewSurface, jpegSurface, previewImageReader.surface)
        cameraDevice?.createCaptureSession(outputs, sessionStateCallback, bgHandler)
    }

    private val captureStateCallback = object : CameraCaptureSession.CaptureCallback() {


    }

    private fun createPreviewRequest(session: CameraCaptureSession) {
        previewSurface?.let {
            val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            requestBuilder?.set(
                CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            requestBuilder?.addTarget(it)
            requestBuilder?.addTarget(previewImageReader.surface)
            val request = requestBuilder?.build()
            request?.let {
                session.setRepeatingRequest(request, captureStateCallback, bgHandler)
            }
        }
    }

    private fun createImageReader() {
        val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        previewSurface?.let {
            builder?.addTarget(it)
        }
        jpegSurface?.let {
            builder?.addTarget(it)
        }
        val request = builder?.build()
        request?.let {
            session?.stopRepeating()
            session?.abortCaptures()
            session?.capture(request, captureStateCallback, bgHandler)
        }
    }


    private fun closeCamera() {
        cameraDevice?.close()
        cameraDevice = null
    }

    private val captureResults: BlockingQueue<CaptureResult> = LinkedBlockingDeque()

    private inner class OnJpegImageAvailableListener : ImageReader.OnImageAvailableListener {

        override fun onImageAvailable(imageReader: ImageReader) {
            Log.d(TAG, "onImageAvailable")
            val image = imageReader.acquireNextImage()
            val captureResult = captureResults.take()
            if (image != null && captureResult != null) {
                // Save image into sdcard.
            }
        }
    }

    //????????????????????????
    private inner class OnPreviewImageAvailableListener : ImageReader.OnImageAvailableListener {


        override fun onImageAvailable(reader: ImageReader) {
            val image: Image = reader.acquireLatestImage()
            if (image.format == ImageFormat.YUV_420_888) {
                VideoEncoderManager.putData(getYUYVFromImage420(image)!!)
            }
            image.close()
        }
    }


    fun getYUYVFromImage420(image: Image?): ByteArray? {
        try {
            //???????????????????????????YUV???????????????planes.length = 3
            //plane[i]?????????????????????????????????byte[].length <= capacity (??????????????????)
            val planes = image!!.planes

            //???????????????????????????????????????width <= rowStride??????????????????byte[].length <= capacity?????????
            //??????????????????width??????
            val width = image.width
            val height = image.height
            //???????????????????????????YUV???????????????2???????????????????????????Y U V ????????? 4:2:2
            val yuvBytes =
                ByteArray(width * height * ImageFormat.getBitsPerPixel(ImageFormat.YUV_422_888) / 8)

            //????????????y u v?????????
            val yBytes = ByteArray(width * height)

            //--??????Y??????
            var buffer = planes[0].buffer
            buffer[yBytes]
            val uPixelsStride: Int
            val vPixelsStride: Int
            //--??????U??????
            uPixelsStride = planes[1].pixelStride
            buffer = planes[1].buffer
            val uTempBytes = ByteArray(buffer.capacity())
            buffer[uTempBytes]
            vPixelsStride = planes[2].pixelStride
            //--??????V??????
            buffer = planes[2].buffer
            val vTempBytes = ByteArray(buffer.capacity())
            buffer[vTempBytes]
            var ySrcIndex = 0
            var uSrcIndex = 0
            var vSrcIndex = 0
            var yDstIndex = 0
            for (j in 0 until height / 2) {
                for (k in 0 until width / 2) {
                    // Y
                    yuvBytes[yDstIndex] = yBytes[ySrcIndex]
                    yuvBytes[yDstIndex + width * 2] = yBytes[ySrcIndex + width]
                    yDstIndex++
                    ySrcIndex++

                    // U
                    yuvBytes[yDstIndex] = uTempBytes[uSrcIndex]
                    yuvBytes[yDstIndex + width * 2] = uTempBytes[uSrcIndex]
                    yDstIndex++

                    // Y
                    yuvBytes[yDstIndex] = yBytes[ySrcIndex]
                    yuvBytes[yDstIndex + width * 2] = yBytes[ySrcIndex + width]
                    yDstIndex++
                    ySrcIndex++

                    // V
                    yuvBytes[yDstIndex] = vTempBytes[vSrcIndex]
                    yuvBytes[yDstIndex + width * 2] = vTempBytes[vSrcIndex]
                    yDstIndex++
                    uSrcIndex += uPixelsStride
                    vSrcIndex += vPixelsStride
                }
                yDstIndex += width * 2
                ySrcIndex += width
            }
            image.close()
            return yuvBytes
        } catch (e: Exception) {
            image?.close()
            Log.e("ArMn", Log.getStackTraceString(e))
        }
        return null
    }


    override fun onPause() {
        super.onPause()
        closeCamera()

    }

}

