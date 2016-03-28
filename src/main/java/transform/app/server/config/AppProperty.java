package transform.app.server.config;

import transform.app.server.common.XmlProperty;

import java.io.IOException;

/**
 * @author malongbo
 */
public class AppProperty {
    private XmlProperty property;
    private String propertyName = "configure.xml";
    private static AppProperty instance = new AppProperty();

    public AppProperty() {
    }

    public AppProperty(String propertyName) {
        this.propertyName = propertyName;
    }


    public static AppProperty me() {
        return instance;

    }

    protected AppProperty init() {
        try {
            String fileName = AppProperty.class.getResource("/" + propertyName).toURI().getPath();
            // System.out.println("fileName => " + fileName);
            property = new XmlProperty(fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    protected void destroy() {
        property.destroy();
        property = null;
    }

    public String getProperty(String key) {
        if (property == null) {
            return null;
        }
        return property.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value;

    }

    public Integer getPropertyToInt(String key, Integer defaultValue) {
        try {
            return Integer.valueOf(getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Integer getPropertyToInt(String key) {
        return getPropertyToInt(key, null);
    }

    public Double getPropertyToDouble(String key, Double defaultValue) {
        try {
            return Double.valueOf(getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Double getPropertyToDouble(String key) {
        return getPropertyToDouble(key, null);
    }

    public Float getPropertyToFloat(String key, Float defaultValue) {
        try {
            return Float.valueOf(getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Float getPropertyToFloat(String key) {
        return getPropertyToFloat(key, null);
    }

    public Boolean getPropertyToBoolean(String key, Boolean defaultValue) {
        try {
            return Boolean.valueOf(getProperty(key));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public Boolean getPropertyToBoolean(String key) {
        return getPropertyToBoolean(key, null);
    }

    public String resourcePrefix() {
        return getProperty(AppConstant.RES_PREFIX);
    }

    public int appPath() {
        return getPropertyToInt(AppConstant.RES_APP_PATH, 0);
    }

    public String uploadRootPath() {
        return getProperty(AppConstant.RES_UPLOAD_ROOT_PATH, "attached");
    }

    public String imagePath() {
        return getProperty(AppConstant.RES_IMAGE_PATH, "/images");
    }

    public String videoPath() {
        return getProperty(AppConstant.RES_VIDEO_PATH, "/videos");
    }

    public String otherPath() {
        return getProperty(AppConstant.RES_OTHER_PATH, "/others");
    }

    public String defaultUserAvatar() {
        return getProperty(AppConstant.RES_DEFAULT_USER_AVATAR, "/images/defaultUserAvatar.jpg");
    }
}
