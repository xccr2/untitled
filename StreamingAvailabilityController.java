public class StreamingAvailabilityController {
    import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

    @RestController
    public class StreamingAvailabilityController {

        private final StreamingAvailabilityService streamingAvailabilityService;

        public StreamingAvailabilityController(StreamingAvailabilityService streamingAvailabilityService) {
            this.streamingAvailabilityService = streamingAvailabilityService;
        }

        @GetMapping("/availability")
        public AvailabilityResponse getAvailability(@RequestParam String title) {
            return streamingAvailabilityService.getAvailability(title);
        }
        @ExceptionHandler(NotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public String handleNotFoundException(NotFoundException e) {
            return e.getMessage();
    }
}
