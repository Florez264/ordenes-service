package com.subocol.taller.ordenes.infrastructure.web.response;

public record ApiResponse<T>(int status, T data) {}
