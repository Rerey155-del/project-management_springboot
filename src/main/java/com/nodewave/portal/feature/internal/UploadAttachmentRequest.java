package com.nodewave.portal.feature.internal;

import jakarta.validation.constraints.NotBlank;

public record UploadAttachmentRequest(
        @NotBlank(message = "attachmentUrl is required")
        String attachmentUrl
) {}
