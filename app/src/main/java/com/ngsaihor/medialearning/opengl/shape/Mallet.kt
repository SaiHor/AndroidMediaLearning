package com.ngsaihor.medialearning.opengl.shape

import android.util.Log
import com.ngsaihor.medialearning.opengl.shader.BaseShaderProgram
import com.ngsaihor.medialearning.opengl.shader.ColorShaderProgram
import com.ngsaihor.medialearning.opengl.shape.model.Point
import com.ngsaihor.medialearning.opengl.util.MalletBuilder
import com.ngsaihor.medialearning.opengl.util.VertexArray

class Mallet(radius: Float,val height: Float, numPointsAroundPuck: Int) : BaseShape() {

    companion object {
        private const val POSITION_COMPONENT_COUNT = 3
    }

    private val generatedData: MalletBuilder.GeneratedData by lazy {
        MalletBuilder.createMallet(Point(0f, 0f, 0f), radius, height, numPointsAroundPuck)
    }

    private val vertexArray: VertexArray by lazy {
        VertexArray(generatedData.vertexData)
    }

    override fun bindData(program: BaseShaderProgram) {
        super.bindData(program)
        if (program is ColorShaderProgram) {
            vertexArray.setVertexAttribPointer(
                0,
                program.getPositionAttribLocation(),
                POSITION_COMPONENT_COUNT,
                0
            )
        }

    }

    override fun draw() {
        super.draw()
        Log.d("GLES20","eneratedData.drawList size:${generatedData.drawList.size}")
        generatedData.drawList.forEach {
            it.draw()
        }
    }
}