export interface ErrorResponseDTO {
    message: string;
    timestamp: string;
    fieldErrors?: Record<string, string>;
}
