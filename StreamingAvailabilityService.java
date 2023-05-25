import com.google.gson.Gson;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class StreamingAvailabilityService {

    private static final String JUSTWATCH_API_BASE_URL = "https://apis.justwatch.com/content/titles/";
    private static final String JUSTWATCH_API_LOCALE = "en_US";

    private final OkHttpClient httpClient;
    private final Gson gson;

    public StreamingAvailabilityService(OkHttpClient httpClient, Gson gson) {
        this.httpClient = httpClient;
        this.gson = gson;
    }

    public AvailabilityResponse getAvailability(String title) {
        // Search for the title on JustWatch API
        String searchUrl = JUSTWATCH_API_BASE_URL + "search?language=en&query=" + title;
        Request searchRequest = new Request.Builder().url(searchUrl).get().build();

        try (Response searchResponse = httpClient.newCall(searchRequest).execute()) {
            if (!searchResponse.isSuccessful()) {
                throw new IOException("Failed to fetch data from JustWatch API: " + searchResponse);
            }

            String searchResponseBody = searchResponse.body().string();
            SearchResult searchResult = gson.fromJson(searchResponseBody, SearchResult.class);

            if (searchResult.getItems().isEmpty()) {
                throw new NotFoundException("Title not found");
            }

            // Get the first result's ID and fetch its details
            int titleId = searchResult.getItems().get(0).getId();
            String detailsUrl = JUSTWATCH_API_BASE_URL + "movie/" + titleId + "/locale/" + JUSTWATCH_API_LOCALE;
            Request detailsRequest = new Request.Builder().url(detailsUrl).get().build();

            try (Response detailsResponse = httpClient.newCall(detailsRequest).execute()) {
                if (!detailsResponse.isSuccessful()) {
                    throw new IOException("Failed to fetch details from JustWatch API: " + detailsResponse);
                }

                String detailsResponseBody = detailsResponse.body().string();
                TitleDetails titleDetails = gson.fromJson(detailsResponseBody, TitleDetails.class);

                // Extract availability information from the TitleDetails object
                AvailabilityResponse availabilityResponse = new AvailabilityResponse();
                availabilityResponse.setTitle(titleDetails.getTitle());
                availabilityResponse.setPlatforms(getPlatformAvailability(titleDetails));

                return availabilityResponse;
            }
        } catch (IOException e) {
            throw new RuntimeException("Error fetching data from JustWatch API", e);
        }
    }

    private Map<String, Boolean> getPlatformAvailability(TitleDetails titleDetails) {
        Map<String, Boolean> platformAvailability = new HashMap<>();

        for (TitleDetails.Offer offer : titleDetails.getOffers()) {
            String platform = offer.getProviderId();
            platformAvailability.put(platform, true);
        }

        return platformAvailability;
    }
}