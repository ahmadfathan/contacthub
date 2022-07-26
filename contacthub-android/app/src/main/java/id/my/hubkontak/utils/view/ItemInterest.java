package id.my.hubkontak.utils.view;

public class ItemInterest {
    String id,description;

    boolean checkbox,chkvisible;
    public ItemInterest() {
    }

    public ItemInterest(String id, String description, boolean checkbox, boolean chkvisible) {
        this.id = id;
        this.description = description;
        this.checkbox = checkbox;
        this.chkvisible = chkvisible;
    }

    public ItemInterest(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }

    public boolean isChkvisible() {
        return chkvisible;
    }

    public void setChkvisible(boolean chkvisible) {
        this.chkvisible = chkvisible;
    }

}
