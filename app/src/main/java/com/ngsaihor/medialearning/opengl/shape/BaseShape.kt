package com.ngsaihor.medialearning.opengl.shape

import com.ngsaihor.medialearning.opengl.shader.BaseShaderProgram

abstract class BaseShape {
    open fun bindData(program:BaseShaderProgram){

    }

    open fun draw(){

    }
}