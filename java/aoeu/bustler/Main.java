package aoeu.bustler;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Activity {

  PackageManager packageManager;
  Typeface font;
  Set<Name> favorites;
  Filename savedFavorites = new Filename("/sdcard/apps.txt");

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
  class Filename extends Runes { Filename(String s) { super(s); }}

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
    List<App> values;

    Apps(List<App> a) {
      super(Main.this, R.layout.app_list_item, a);
      this.values = a;
    }

    @Override
    public
    View getView(int index, View v, ViewGroup parent) {
      if (v == null) {
        v = inflateListItem(parent);
      }
      if (index >= values.size()) return v;
      App app = values.get(index);
      if (app == null) return v;
      setText(v, app.colloquialName);
      setClickListener(v, index);
      setLongClickListener(v, index);
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
          values.get(index).start();
        }
      });
    }

    void setLongClickListener(View v, final int index) {
      v.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View view) {
          Name n = values.get(index).colloquialName;
          if (favorites.contains(n)) {
            favorites.remove(n);
          } else {
            favorites.add(n);
          }
          save(savedFavorites, favorites);
          prioritizedAppsView.setAdapter(allApps.filter(favorites));
          return true;
        }
      });
    }

    void toast(String s) {
      android.widget.Toast.makeText(Main.this, s, android.widget.Toast.LENGTH_LONG).show();
    }

    Apps filter(Set<Name> s) {
      List<App> f = new ArrayList<>();
      for (App a : values) {
        if (s.contains(a.colloquialName)) f.add(a);
      }
      return new Apps(f);
    }
  }

  @Override
  public
  void onCreate(Bundle b) {
    super.onCreate(b);
    init();
  }

  Apps allApps;
  ListView prioritizedAppsView;

  void init() {
    packageManager = getPackageManager();
    font = getFont(new AssetPath("Go-Regular.ttf"));
    favorites = loadFavoriteAppNames(savedFavorites);
    setContentView(R.layout.main);
    allApps = new Apps(newAppList());
    ((ListView) of(R.id.allApps)).setAdapter(allApps);
    prioritizedAppsView = (ListView) of(R.id.prioritizedApps);
    prioritizedAppsView.setAdapter(allApps.filter(favorites));
  }

  Typeface getFont(AssetPath a) {
    try {
      return Typeface.createFromAsset(getAssets(), a.value);
    } catch (RuntimeException exceptionForAssetPathNotFound) {
      return Typeface.DEFAULT;
    }
  }

  View of(int ID) {
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

  Set<Name> loadFavoriteAppNames(Filename f ) {
    Set<Name> s = new HashSet<>();
    for (String n : read(f)) {
       s.add(new Name(n));
     }
     return s;
  }

  List<String> read(Filename f) {
    try {
      return Arrays.asList(
        new String(Files.readAllBytes(Paths.get(f.value))).split("\n")
      );
    } catch (Exception err) {
      android.util.Log.w("aoeu", "Couldn't read favorite apps: " + err);
      return new ArrayList<>();
    }
  }

  void save(Filename f, Set<Name> apps) {
    String s = "";
    for (Name n : apps) {
      s += n.value + "\n";
    }
    write(f, s);
  }

  void write(Filename f, String s) {
    try {
      Files.write(Paths.get(f.value), s.getBytes());
    } catch (Exception err) {
      android.util.Log.w("aoeu", "Couldn't write favorite apps: " + err);
    }
  }
}
