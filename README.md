[![Android Arsenal]( https://img.shields.io/badge/Android%20Arsenal-MapperJ-green.svg?style=flat )]( https://android-arsenal.com/details/1/7153 )

# MapperJ
MapperJ is an annotation processing library which helps you get rid of manual creating of mapper classes and it has little bonus.

I hope everybody uses Clean Architecture approaches for creating theirs Android projects now.
Who uses this approaches needs to write boring mapper classes which transfer data between layers such as:
- db source (Entities models to Domain models and back)
- net source (Responses models to Domain models)
- domain (Domain models to UI models)

So this annotation processing code helps you get rid of such works (different way of MapStruct).

## Installation

Add to the `build.gradle` of your app module:
```Groovy
dependencies {
    implementation 'com.github.sashamerkulev:MapperJ-annotations:1.0.2'
    kapt 'com.github.sashamerkulev:MapperJ-processors:1.0.2'
    annotationProcessor 'com.github.sashamerkulev:MapperJ-processors:1.0.2'
}
```

## Usage

``` kotlin
@Mapper(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class])
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
@Mapper(twoWayMapClasses = [DbEntity::class], oneWayMapClasses = [ModelResponse::class]) 
```

and there is a generated code which I mentioned before:
``` java
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

# Args

Args is an annotation generates you a class which helps you to transfer data to Intent or Bundle and back and it is comfortable way for transfering arguments to fragments, activities, services and etc.

## Usage

``` kotlin
@Args(source = Source.Kotlin)
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
data class BundleModelArgs(
    val id: Int,
    val name: String,
    val shrt: Short,
    val lng: Long,
    val bol: Boolean,
    val bte: Byte,
    val foat: Float,
    val dbl: Double
) {
    fun toIntent(): Intent {
        val intent = Intent()
        intent.putExtra("id", id)
        intent.putExtra("name", name)
        intent.putExtra("shrt", shrt)
        intent.putExtra("lng", lng)
        intent.putExtra("bol", bol)
        intent.putExtra("bte", bte)
        intent.putExtra("foat", foat)
        intent.putExtra("dbl", dbl)
        return intent
    }

    fun toBundle(): Bundle {
        val bundle = Bundle()
        bundle.putInt("id", id)
        bundle.putString("name", name)
        bundle.putShort("shrt", shrt)
        bundle.putLong("lng", lng)
        bundle.putBoolean("bol", bol)
        bundle.putByte("bte", bte)
        bundle.putFloat("foat", foat)
        bundle.putDouble("dbl", dbl)
        return bundle
    }

    companion object {
        @JvmStatic
        fun fromIntent(intent: Intent): BundleModelArgs {
            return BundleModelArgs(
                intent.getIntExtra("id", 0),
                intent.getStringExtra("name"),
                intent.getShortExtra("shrt", 0),
                intent.getLongExtra("lng", 0),
                intent.getBooleanExtra("bol", false),
                intent.getByteExtra("bte", 0),
                intent.getFloatExtra("foat", 0F),
                intent.getDoubleExtra("dbl", 0.0)
            )
        }

        @JvmStatic
        fun fromBundle(bundle: Bundle): BundleModelArgs {
            return BundleModelArgs(
                bundle.getInt("id", 0),
                bundle.getString("name"),
                bundle.getShort("shrt", 0),
                bundle.getLong("lng", 0),
                bundle.getBoolean("bol", false),
                bundle.getByte("bte", 0),
                bundle.getFloat("foat", 0F),
                bundle.getDouble("dbl", 0.0)
            )
        }

    }
}

```

### ToDo list

* [x] Java and Kotlin code generation
* [ ] Ignore for Args
* [ ] Default values for Args and Mapper

License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
