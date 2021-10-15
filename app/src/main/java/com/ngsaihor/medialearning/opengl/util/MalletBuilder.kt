package com.ngsaihor.medialearning.opengl.util

import android.opengl.GLES20
import android.opengl.GLES20.GL_TRIANGLE_FAN
import android.opengl.GLES20.glDrawArrays
import com.ngsaihor.medialearning.opengl.shape.model.Circle
import com.ngsaihor.medialearning.opengl.shape.model.Cylinder
import com.ngsaihor.medialearning.opengl.shape.model.Point
import java.lang.Math.cos
import java.lang.Math.sin


class MalletBuilder private constructor(sizeInVertices: Int) {

    companion object {
        private const val FLOAT_PER_VERTEX = 3

        private fun sizeOfCircleInVertices(numPoints: Int) = numPoints + 2

        private fun sizeOfOpenCylinderInVertices(numPoints: Int) = (numPoints + 1) * 2

        fun createMallet(
            center: Point,
            radius: Float,
            height: Float,
            numPoints: Int
        ): GeneratedData {
            val size = (sizeOfCircleInVertices(numPoints) * 2
                    + sizeOfOpenCylinderInVertices(numPoints) * 2)
            val builder = MalletBuilder(size)
            //底座
            val baseHeight = height * 0.25f
            val baseCircle = Circle(
                center.translateY(-baseHeight),
                radius
            )
            val baseCylinder = Cylinder(
                baseCircle.centerPoint.translateY(-baseHeight / 2f),
                radius, baseHeight
            )
            builder.appendCircle(baseCircle, numPoints)
            builder.appendCylinder(baseCylinder, numPoints)


            //手柄
            val handleHeight = height * 0.75f
            val handleRadius = radius / 3f
            val handleCircle = Circle(
                center.translateY(height * 0.5f),
                handleRadius)
            val handleCylinder =
                Cylinder(
                    handleCircle.centerPoint.translateY(-handleHeight / 2),
                    handleRadius,
                    handleHeight
                )
            builder.appendCircle(handleCircle, numPoints)
            builder.appendCylinder(handleCylinder, numPoints)

            return builder.build()
        }


        fun createPuck(puck: Cylinder, numPoints: Int): GeneratedData {
            val size = (sizeOfCircleInVertices(numPoints)
                    + sizeOfOpenCylinderInVertices(numPoints))
            val builder = MalletBuilder(size)
            val puckTop = Circle(
                puck.centerPoint.translateY(puck.height / 2f),
                puck.radius
            )
            builder.appendCircle(puckTop, numPoints)
            builder.appendCylinder(puck, numPoints)
            return builder.build()
        }
    }

    interface DrawCommand {
        fun draw()
    }

    class GeneratedData internal constructor(
        val vertexData: FloatArray,
        val drawList: List<DrawCommand>
    )

    private fun build(): GeneratedData {
        return GeneratedData(vertexData, drawList)
    }

    private var vertexData: FloatArray = FloatArray(sizeInVertices * FLOAT_PER_VERTEX)
    private val drawList: MutableList<DrawCommand> = ArrayList()
    private var offset = 0

    private fun appendCircle(circle: Circle, numPoints: Int) {
        val startVertex = offset / FLOAT_PER_VERTEX
        val numVertex = sizeOfCircleInVertices(numPoints)

        //圆心
        vertexData[offset++] = circle.centerPoint.x
        vertexData[offset++] = circle.centerPoint.y
        vertexData[offset++] = circle.centerPoint.z

        for (index in 0..numPoints) {
            val angleInRadians = ((index.toFloat() / (numPoints.toFloat()) * (Math.PI * 2f)))
            vertexData[offset++] = (circle.centerPoint.x
                    + circle.radius * cos(angleInRadians)).toFloat()
            vertexData[offset++] = circle.centerPoint.y
            vertexData[offset++] = (circle.centerPoint.z
                    + circle.radius * sin(angleInRadians)).toFloat()
        }
        drawList.add(object : DrawCommand {
            override fun draw() {
                glDrawArrays(GL_TRIANGLE_FAN, startVertex, numVertex)
            }
        })
    }

    private fun appendCylinder(cylinder: Cylinder, numPoints: Int) {
        val startVertex = offset / FLOAT_PER_VERTEX
        val numVertex = sizeOfOpenCylinderInVertices(numPoints)
        val yStart = cylinder.centerPoint.y - (cylinder.height / 2f)
        val yEnd = cylinder.centerPoint.y + (cylinder.height / 2f)
        vertexData.let {
            for (index in 0..numPoints) {
                val angleInRadians = ((index.toFloat() / (numPoints.toFloat()) * (Math.PI * 2f)))
                val xPosition = (cylinder.centerPoint.x
                        + cylinder.radius * cos(angleInRadians)).toFloat()
                val zPosition = (cylinder.centerPoint.z
                        + cylinder.radius * sin(angleInRadians)).toFloat()
                it[offset++] = xPosition
                it[offset++] = yStart
                it[offset++] = zPosition

                it[offset++] = xPosition
                it[offset++] = yEnd
                it[offset++] = zPosition
            }
            drawList.add(object : DrawCommand {
                override fun draw() {
                    glDrawArrays(GLES20.GL_TRIANGLE_STRIP, startVertex, numVertex)
                }
            })
        }
    }

}