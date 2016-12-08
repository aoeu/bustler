package aoeu.bustler;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main extends ListActivity {

  PackageManager packageManager;

  class Value<Type> {
    final Type value;
    Value(Type value) {
      this.value = value;
    }
  }

  class Name extends Value<String> { Name(String s) { super(s); }}
  class Package extends Value<String> { Package(String s) { super(s); }}

  class App {
    final Name colloquialName;
    final Package javaPackage;

    App(ResolveInfo i) {
      this.colloquialName = new Name(i.loadLabel(packageManager).toString());
      this.javaPackage = new Package(i.activityInfo.packageName);
    }

    Intent createIntentToLaunchMainActivity() {
      return packageManager.getLaunchIntentForPackage(javaPackage.value);
    }

    void start() {
      startActivity(createIntentToLaunchMainActivity());
    }
  }

  class Apps extends ArrayAdapter<App> {
    List<App> apps;

    Apps(Context c, int resourceID, List<App> apps) {
      super(c, resourceID, apps);
      this.apps = apps;
    }

    @Override
    public
    @NonNull
    View getView(int index, View v, @NonNull ViewGroup parent) {
      if (v == null) {
        v = inflateListItem(parent);
      }
      if (index >= apps.size()) return v;
      App app = apps.get(index);
      if (app == null) return v;
      getAppNameTextView(v).setText(app.colloquialName.value);
      return v;
    }

    App get(int index) {
      return getItem(index);
    }

    View inflateListItem(ViewGroup parent) {
      return getLayoutInflater().inflate(R.layout.app_list_item, parent, false);
    }

    TextView getAppNameTextView(View listItem) {
      return ((TextView)listItem.findViewById(R.id.appName));
    }
  }

  Apps apps;

  @Override
  public
  void onCreate(Bundle b) {
    super.onCreate(b);
    init();
  }

  @Override
  protected
  void onListItemClick(ListView l, View v, int index, long id) {
    super.onListItemClick(l, v, index, id);
    this.apps.get(index).start();
  }

  void init() {
    packageManager = getPackageManager();
    setContentView(R.layout.main);
    apps = new Apps(this, R.layout.app_list_item, createApps());
    setListAdapter(apps);
  }

  List<App> createApps() {
    List<App> l = new ArrayList<>();
    for(ResolveInfo i : getActivitiesThatCanBeLaunched()) {
      l.add(new App(i));
    }
    return sort(l);
  }

  List<ResolveInfo> getActivitiesThatCanBeLaunched() {
    final int bitwiseFlagsUsedAsQueryFilter = 0;
    return packageManager.queryIntentActivities(
        new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),
        bitwiseFlagsUsedAsQueryFilter
    );
  }

  static List<App> sort(List<App> l) {
    Collections.sort(l, new Comparator<App>() {
      @Override
      public int compare(App a, App b) {
        return a.colloquialName.value.compareTo(b.colloquialName.value);
      }
    });
    return l;
  }
}
