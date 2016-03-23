package transform.app.server.common.bean;

import com.jfinal.plugin.activerecord.Page;
import transform.app.server.model.Venue;

import java.util.List;

public class VenueDetailVO {
    private Venue venue;
    private List<?> venueSports;
    private Page<?> venueComments;

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public List<?> getVenueSports() {
        return venueSports;
    }

    public void setVenueSports(List<?> venueSports) {
        this.venueSports = venueSports;
    }

    public Page<?> getVenueComments() {
        return venueComments;
    }

    public void setVenueComments(Page<?> venueComments) {
        this.venueComments = venueComments;
    }
}
