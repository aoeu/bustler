package aoeu.launcher;

import android.app.ListActivity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Main extends ListActivity {

  class App {
    final CharSequence name;
    final String className;
    final String packageName;

    App(ApplicationInfo a) {
      this.name = a.loadLabel(getPackageManager());
      this.className = a.className;
      this.packageName = a.packageName;
    }
  }

  class Adapter extends ArrayAdapter<App> {
    List<App> apps;

    Adapter(Context c, int resourceID, List<App> apps) {
      super(c, resourceID, apps);
      this.apps = apps;
    }

    @Override
    public
    @NonNull
    View getView(int index, View v, ViewGroup parent) {
      if (v == null) {
        v = getLayoutInflater().inflate(R.layout.app_list_item, null);
      }
      if (index >= apps.size()) return v;
      App app = apps.get(index);
      if (app == null) return v;
      ((TextView)v.findViewById(R.id.appName)).setText(app.name);
      return v;
    }
  }

  @Override public void onCreate(Bundle b) {
    super.onCreate(b);
    this.setContentView(R.layout.main);
    this.setListAdapter(new Adapter(this, R.layout.app_list_item, initApps()));
  }

  @Override protected void onListItemClick(ListView l, View v, int index, long id) {
    super.onListItemClick(l, v, index, id);
    App a = (App)getListAdapter().getItem(index);
    startActivity(getPackageManager().getLaunchIntentForPackage(a.packageName));
  }

  List<App> initApps() {
    List<App> apps = new ArrayList<>();
    for(ApplicationInfo a : getPackageManager().getInstalledApplications(PackageManager.GET_META_DATA)) {
      apps.add(new App(a));
    }
    return apps;
  }

}