package com.icyrockton.xjson.runtime.exception

class MissingPropertyException(propName: String) : IllegalArgumentException(
    "property $propName is required, but it was missing"
)