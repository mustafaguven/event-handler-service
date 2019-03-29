package com.mg.eventbus.exception

import com.mg.eventbus.gateway.Commandable

class CommandTimeoutException(command: Commandable, timeout: Long) :
        Exception("Command (${command.javaClass.simpleName}: ${command.uuid}) is not processed in given timeout duration ($timeout milliseconds)")