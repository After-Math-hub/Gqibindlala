package com.gqibindlala.booking.dto;

public record CloudinaryUploadSignature(String cloudName, String apiKey, long timestamp,
                                        String folder, String signature) {}