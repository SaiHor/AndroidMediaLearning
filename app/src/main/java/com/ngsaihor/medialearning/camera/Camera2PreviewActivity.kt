package com.ngsaihor.medialearning.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.core.app.ActivityCompat
import com.ngsaihor.medialearning.databinding.ActivityCamera2PreviewBinding
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingDeque

class Camera2PreviewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCamera2PreviewBinding

    private lateinit var cameraManager: CameraManager
    private lateinit var imageReader: ImageReader
    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null

    private var cameraId: String = ""

    private var previewSurfaceTexture: SurfaceTexture? = null
    private var previewSurface: Surface? = null
    private var jpegSurface: Surface? = null

    private lateinit var cameraCharacteristics: CameraCharacteristics

    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamera2PreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.capture.setOnClickListener {
            createImageReader()
        }
        startBackgroundThread()
        initCamera()
    }

    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        bgThread = HandlerThread("CameraBackground").also { it.start() }
        bgHandler = bgThread?.looper?.let { Handler(it) }
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        bgThread?.quitSafely()
        try {
            bgThread?.join()
            bgThread = null
            bgHandler = null
        } catch (e: InterruptedException) {

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
        val streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val supportedSizes = streamConfigurationMap?.getOutputSizes(clazz)
        if (supportedSizes != null) {
            for (size in supportedSizes) {
                if (size.width.toFloat() / size.height == aspectRatio && size.height <= maxHeight && size.width <= maxWidth) {
                    return size
                }
            }
        }
        return null
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


    private fun createCameraCaptureSession() {
        val imageSize = getOptimalSize(
            cameraCharacteristics,
            SurfaceTexture::class.java,
            binding.textureView.measuredWidth,
            binding.textureView.measuredHeight
        )
        imageSize?.let {
            imageReader = ImageReader.newInstance(imageSize.width, imageSize.height, ImageFormat.NV21, 5)
            imageReader.setOnImageAvailableListener(OnJpegImageAvailableListener(), bgHandler)
            jpegSurface = imageReader.surface
        }
        val sessionStateCallback = SessionStateCallback()
        val outputs = listOf(previewSurface, jpegSurface)
        cameraDevice?.createCaptureSession(outputs, sessionStateCallback, bgHandler)
    }

    private fun createPreviewRequest(session: CameraCaptureSession) {
        previewSurface?.let {
            val requestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            requestBuilder?.addTarget(it)
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

    private val captureStateCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)

        }
    }

    private inner class OnJpegImageAvailableListener : ImageReader.OnImageAvailableListener {

        override fun onImageAvailable(imageReader: ImageReader) {
            val image = imageReader.acquireNextImage()
            val captureResult = captureResults.take()
            if (image != null && captureResult != null) {
                // Save image into sdcard.
            }
        }
    }


    private val cameraStateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(cameraDevice: CameraDevice) {
            this@Camera2PreviewActivity.cameraDevice = cameraDevice
        }

        override fun onDisconnected(cameraDevice: CameraDevice) {

        }

        override fun onError(cameraDevice: CameraDevice, error: Int) {

        }

    }

    private inner class SessionStateCallback : CameraCaptureSession.StateCallback() {

        override fun onConfigureFailed(session: CameraCaptureSession) {

        }


        override fun onConfigured(session: CameraCaptureSession) {
            Log.d("xxx", "onConfigured success ")
            this@Camera2PreviewActivity.session = session
            createPreviewRequest(session)
        }


        override fun onClosed(session: CameraCaptureSession) {

        }
    }

    private inner class PreviewSurfaceTextureListener : TextureView.SurfaceTextureListener {

        override fun onSurfaceTextureSizeChanged(surfaceTexture: SurfaceTexture, width: Int, height: Int) {

        }


        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {

        }


        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {

            return true
        }


        override fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {
            previewSurfaceTexture = surfaceTexture
            val previewSize = getOptimalSize(cameraCharacteristics, SurfaceTexture::class.java, width, height)
            previewSize?.let {
                previewSurfaceTexture?.setDefaultBufferSize(previewSize.width, previewSize.height)
                previewSurface = Surface(surfaceTexture)
                createCameraCaptureSession()
            }

        }
    }

    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    override fun onResume() {
        super.onResume()
//        openCamera()
    }
}

