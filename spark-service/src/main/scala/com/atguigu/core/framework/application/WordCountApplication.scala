package com.atguigu.core.framework.application

import com.atguigu.core.framework.common.TApplication
import com.atguigu.core.framework.controller.WordCountController

object WordCountApplication extends App with TApplication{

    // 启动应用程序
    start(){
        val controller = new WordCountController()
        controller.dispatch()
    }

}
