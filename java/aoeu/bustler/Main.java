package aoeu.bustler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Main extends Activity {

  PackageManager packageManager;
  Typeface font;

  class Value<Type> {
    final Type value;
    Value(Type value) {
      this.value = value;
    }
    public boolean equals(Object o) {
      return (o == this) ||  (o != null && getClass().isInstance(o) && ((Value)o).value.equals(value));
    }
    public int hashCode() {
      return value.hashCode();
    }
  }

  class Runes extends Value<String> { Runes(String s) { super(s == null ? "" : s); }}

  class AssetPath extends Runes { AssetPath(String s) { super(s); }}

  class Name extends Runes { Name(String s) { super(s); }}
  class Package extends Runes { Package(String s) { super(s); }}

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

    Apps(List<App> a) {
      super(Main.this, R.layout.app_list_item, a);
      this.apps = a;
    }

    @Override
    public
    View getView(int index, View v, ViewGroup parent) {
      if (v == null) {
        v = inflateListItem(parent);
      }
      if (index >= apps.size()) return v;
      App app = apps.get(index);
      if (app == null) return v;
      setText(v, app.colloquialName);
      setClickListener(v, index);
      return v;
    }

    View inflateListItem(ViewGroup parent) {
      return getLayoutInflater().inflate(R.layout.app_list_item, parent, false);
    }

    void setText(View listItem, Name n) {
      TextView v = ((TextView)listItem.findViewById(R.id.appName));
      if (v == null) return;
      v.setText(n.value);
      v.setTypeface(font);
    }

    void setClickListener(View v, final int index) {
      v.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          apps.get(index).start();
        }
      });
    }
  }

  @Override
  public
  void onCreate(Bundle b) {
    super.onCreate(b);
    init();
  }

  void init() {
    packageManager = getPackageManager();
    font = getFont(new AssetPath("Go-Regular.ttf"));
    setContentView(R.layout.main);
    List<App> a = newAppList();
    ((ListView)_(R.id.allApps)).setAdapter(new Apps(a));
    ((ListView)_(R.id.prioritizedApps)).setAdapter(new Apps(filter(a)));
  }

  Typeface getFont(AssetPath a) {
    try {
      return Typeface.createFromAsset(getAssets(), a.value);
    } catch (RuntimeException exceptionForAssetPathNotFound) {
      return Typeface.DEFAULT;
    }
  }

  View _(int ID) {
    return findViewById(ID);
  }

  List<App> newAppList() {
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

  List<App> filter(List<App> l) {
    Set<Name> p = new HashSet<>();
    p.add(new Name("Camera"));
    p.add(new Name("Signal"));
    p.add(new Name("Firefox"));
    p.add(new Name("Kiwix"));
    p.add(new Name("Maps"));
    p.add(new Name("Phone"));
    p.add(new Name("Wikipedia"));
    p.add(new Name("Spotify"));
    p.add(new Name("Inbox"));

    List<App> f = new ArrayList<>();
    for (App a : l) {
      if (p.contains(a.colloquialName)) f.add(a);
    }
    return f;
  }
}
