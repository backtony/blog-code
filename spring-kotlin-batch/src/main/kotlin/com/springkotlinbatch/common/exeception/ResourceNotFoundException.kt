package com.springkotlinbatch.common.exeception

class ResourceNotFoundException(msg: String) : RuntimeException(msg) {

    companion object {
        fun notFound(resourceName: String, resourceId: String): ResourceNotFoundException {
            return ResourceNotFoundException("$resourceName not found. id: $resourceId")
        }
    }
}

