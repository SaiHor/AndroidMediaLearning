package com.ngsaihor.medialearning.opengl

import android.content.Context
import android.opengl.GLES20.*
import android.util.Log
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader


fun createProgramByShaderArray(
    context: Context,
    verTextResIdList: List<Int>,
    fragmentResIdList: List<Int>
): Int {
    val program = glCreateProgram()
    if (program == 0) {
        Log.d("GLES20", "glCreateProgram 创建失败")
        return 0
    }
    verTextResIdList.forEach {
        loadVertexShaderFromResource(context, it).takeIf { shader -> shader > 0 }?.apply {
            glAttachShader(program, this)
        }
    }
    fragmentResIdList.forEach {
        loadFragmentShaderFromResource(context, it).takeIf { shader -> shader > 0 }?.apply {
            glAttachShader(program, this)
        }
    }
    glLinkProgram(program)
    val compileStatus = IntArray(1)
    glGetProgramiv(program, GL_LINK_STATUS, compileStatus, 0)
    Log.d("GLES20", "programID:$program complie info:${glGetProgramInfoLog(program)}")
    if (compileStatus[0] == 0){
        glDeleteProgram(program)
        Log.d("GLES20", "program 链接失败")
        return 0
    }
    return program
}

fun loadVertexShaderFromResource(context: Context, resId: Int): Int {
    return loadShaderFromResource(context, GL_VERTEX_SHADER, resId)
}

fun loadFragmentShaderFromResource(context: Context, resId: Int): Int {
    return loadShaderFromResource(context, GL_FRAGMENT_SHADER, resId)
}

fun loadShaderFromResource(context: Context, type: Int, resId: Int): Int {
    val shader = glCreateShader(type)
    if (shader == 0) {
        Log.d("GLES20", "glCreateShader 创建失败")
        return 0
    }
    glShaderSource(shader, readShaderTextFromResource(context, resId))
    glCompileShader(shader)
    val compileStatus = IntArray(1)
    glGetShaderiv(shader, GL_COMPILE_STATUS, compileStatus, 0)
    Log.d("GLES20", "shaderID:$shader complie info:${glGetShaderInfoLog(shader)}")
    if (compileStatus[0] == 0) {
        glDeleteShader(shader)
        Log.d("GLES20", "shade 编译失败")
        return 0
    }
    return shader
}

private fun readShaderTextFromResource(context: Context, resId: Int): String {
    val body = StringBuilder()
    var inputStream: InputStream? = null
    var bufferReader: BufferedReader? = null
    try {
        inputStream = context.resources.openRawResource(resId)
        bufferReader = BufferedReader(InputStreamReader(inputStream))
        var nextLine: String?
        while (bufferReader.readLine().also { nextLine = it } != null) {
            body.append(nextLine)
            body.append('\n')
        }
    } catch (e: Exception) {
        e.printStackTrace()
    } finally {
        inputStream?.close()
        bufferReader?.close()
    }
    Log.d("GLES20",body.toString())
    return body.toString()
}

fun loadShader(type: Int, shaderCode: String): Int {
    val shader = glCreateShader(type)
    glShaderSource(shader, shaderCode)
    glCompileShader(shader)
    return shader
}