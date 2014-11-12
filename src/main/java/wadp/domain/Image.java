package wadp.domain;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;
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

    @Basic(fetch = FetchType.LAZY)
    @OneToOne
    private FileObject original;

    @Basic(fetch = FetchType.LAZY)
    @OneToOne
    private FileObject galleryThumbnail;

    @Basic(fetch = FetchType.LAZY)
    @OneToOne
    private FileObject postThumbnail;

    private double latitude;
    private double longitude;
    @Temporal(TemporalType.TIMESTAMP)
    private Date captureDate;
    private boolean location;

    public Image() {
        this.location = false;
        this.captureDate = null;
    }

    public FileObject getOriginal() {
        return original;
    }

    public void setOriginal(FileObject original) {
        this.original = original;
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

    public FileObject getGalleryThumbnail() {
        return galleryThumbnail;
    }

    public void setGalleryThumbnail(FileObject galleryThumbnail) {
        this.galleryThumbnail = galleryThumbnail;
    }

    public FileObject getPostThumbnail() {
        return postThumbnail;
    }

    public void setPostThumbnail(FileObject postThumbnail) {
        this.postThumbnail = postThumbnail;
    }

}
