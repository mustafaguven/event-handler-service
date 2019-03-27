package com.mg.eventbus.exception

class CommandTimeoutException(timeout: Long) :
        Exception("Command is not processed in given timeout duration ($timeout milliseconds)")