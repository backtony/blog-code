package com.oauth2.domain.exception

class ResourceNotFoundException(msg: String) : RuntimeException(msg) {

    companion object {
        fun notFound(resourceName: String, resourceId: Long): ResourceNotFoundException {
            return ResourceNotFoundException("$resourceName not found. id: $resourceId")
        }
    }
}

