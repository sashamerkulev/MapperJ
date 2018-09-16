# MapperJ
MapperJ is a annotation processing library which helps you get rid of manual creating of mapper classes and a little bit more.

I hope everybody uses Clean Architecture approaches for creating theirs Android projects now.
Who uses this approaches needs to write boring mapper classes which transfer data between layers such as:
- db source (Entities models to Domain models and back)
- net source (Responses models to Domain models)

So this annotation processing code helps you get rid of such works (different way of MapStruct).

Usage:

``` kotlin
@MapperJ(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class])
data class DomainModel1(
        val id: Int,
        val name: String,
        val x: Float,
        val y: Double,
        val z: Long,
        val child: DomainChild,
        val ab: Short
)

data class DomainChild(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short
)

data class DbEntity(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short,
        val children: List<DbChildEntity>
)

data class DbChildEntity(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short
)

data class ModelResponse(
        val id2: Int,
        val name: String,
        val x: Float,
        val y: Double,
        val child: ChildResponse
)

data class ChildResponse(
        val id: Int,
        val name: String,
        val zz: Byte,
        val ab: Short
)

```
As you can see I used the following annnotation:
``` kotlin 
@MapperJ(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class]) 
```

and there is a generated code which I mentioned before:
``` kotlin
public class DomainModel1Mapper {

    public DomainModel1 mapToDomainModel1(DbEntity item) {
        return new DomainModel1(item.getId(), item.getName(), 0F, 0.0, 0, null, item.getAb());
    }

    public DbEntity mapToDbEntity(DomainModel1 item) {
        return new DbEntity(item.getId(), item.getName(), (byte) 0, item.getAb(), new ArrayList());
    }

    public DomainModel1 mapToDomainModel1(ModelResponse item) {
        return new DomainModel1(0, item.getName(), item.getX(), item.getY(), 0, mapToDomainChild(item.getChild()), (short) 0);
    }

    public DomainChild mapToDomainChild(ChildResponse item) {
        return new DomainChild(
                item.getId(), item.getName(), item.getZz(), item.getAb()
        );
    }

}

```

And little bonus here:
``` kotlin
@Args
data class BundleModel(
        val id: Int,
        val name: String,
        val shrt: Short,
        val lng: Long,
        val bol: Boolean,
        val bte: Byte,
        val foat: Float,
        val dbl: Double
)

```
This annotation generates the following class:

``` kotlin
public class BundleModelArgs {

    private final int id;
    private final String name;
    private final short shrt;
    private final long lng;
    private final boolean bol;
    private final byte bte;
    private final float foat;
    private final double dbl;

    public BundleModelArgs(int id, String name, short shrt, long lng, boolean bol, byte bte, float foat, double dbl) {
        this.id = id;
        this.name = name;
        this.shrt = shrt;
        this.lng = lng;
        this.bol = bol;
        this.bte = bte;
        this.foat = foat;
        this.dbl = dbl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public short getShrt() {
        return shrt;
    }

    public long getLng() {
        return lng;
    }

    public boolean getBol() {
        return bol;
    }

    public byte getBte() {
        return bte;
    }

    public float getFoat() {
        return foat;
    }

    public double getDbl() {
        return dbl;
    }

    public Intent toIntent() {
        Intent intent = new Intent();
        intent.putExtra("id", id);
        intent.putExtra("name", name);
        intent.putExtra("shrt", shrt);
        intent.putExtra("lng", lng);
        intent.putExtra("bol", bol);
        intent.putExtra("bte", bte);
        intent.putExtra("foat", foat);
        intent.putExtra("dbl", dbl);
        return intent;
    }

    public static BundleModelArgs fromIntent(Intent intent) {
        return new BundleModelArgs(
                intent.getIntExtra("id", 0),
                intent.getStringExtra("name"),
                intent.getShortExtra("shrt", (short) 0),
                intent.getLongExtra("lng", 0),
                intent.getBooleanExtra("bol", false),
                intent.getByteExtra("bte", (byte) 0),
                intent.getFloatExtra("foat", 0F),
                intent.getDoubleExtra("dbl", 0.0)
        );
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putInt("id", id);
        bundle.putString("name", name);
        bundle.putShort("shrt", shrt);
        bundle.putLong("lng", lng);
        bundle.putBoolean("bol", bol);
        bundle.putByte("bte", bte);
        bundle.putFloat("foat", foat);
        bundle.putDouble("dbl", dbl);
        return bundle;
    }

    public static BundleModelArgs fromBundle(Bundle bundle) {
        return new BundleModelArgs(
                bundle.getInt("id", 0),
                bundle.getString("name"),
                bundle.getShort("shrt", (short) 0),
                bundle.getLong("lng", 0),
                bundle.getBoolean("bol", false),
                bundle.getByte("bte", (byte) 0),
                bundle.getFloat("foat", 0F),
                bundle.getDouble("dbl", 0.0)
        );
    }

}

```
As you can see this class will help you to transfer data to Intent or Bundle and back which is convinient way for transfer arguments to fragments, activities and etc.

