package com.nodewave.portal.feature.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UploadAttachmentRequest(
        @Schema(description = "URL bukti pengerjaan / attachment", example = "https://nodewave.id/attachments/figma-spec-v1.pdf")
        @NotBlank(message = "attachmentUrl is required")
        String attachmentUrl
) {}
