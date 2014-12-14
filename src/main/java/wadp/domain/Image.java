package wadp.domain;

import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 *
 * @Image contains reference to byteobject and both gallery and postthumnail of picture, image
 * also knows its location and capturetime
 */
@Entity
public class Image extends AbstractPersistable<Long> {

    // postgresql really does not like it when you load large objects like images outside transaction,
    // and lazy fetchtype is merely a recommendation -> random stack traces when framework decides to ignore the
    // fetch type and load image outside @Transactional block
    // hence, we now only store the ids here, and load images separately when we need to show them

    private Long originalId;
    private Long postThumbnailId;
    private Long galleryThumbnailId;

    private double latitude;
    private double longitude;
    @Temporal(TemporalType.TIMESTAMP)
    private Date captureDate;
    private boolean location;

    public Image() {
        this.location = false;
        this.captureDate = null;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public Date getCaptureDate() {
        return captureDate;
    }

    public void setCaptureDate(Date captureDate) {
        this.captureDate = captureDate;
    }

    public boolean getLocation() {
        return location;
    }

    public void setLocation(boolean location) {
        this.location = location;
    }

    public Long getOriginalId() {
        return originalId;
    }

    public void setOriginalId(Long originalId) {
        this.originalId = originalId;
    }

    public Long getPostThumbnailId() {
        return postThumbnailId;
    }

    public void setPostThumbnailId(Long postThumbnailId) {
        this.postThumbnailId = postThumbnailId;
    }

    public Long getGalleryThumbnailId() {
        return galleryThumbnailId;
    }

    public void setGalleryThumbnailId(Long galleryThumbnailId) {
        this.galleryThumbnailId = galleryThumbnailId;
    }
}
