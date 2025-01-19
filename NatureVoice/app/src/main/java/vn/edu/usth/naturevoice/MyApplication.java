package vn.edu.usth.naturevoice;

public class MyApplication extends android.app.Application{
    private boolean hasRun = false;

    public boolean hasRunOnce() {
        return hasRun;
    }

    public void setHasRunOnce(boolean hasRun){
        this.hasRun = hasRun;
    }
}
