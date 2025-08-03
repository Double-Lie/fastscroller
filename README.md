# fastscroller
multiusable fastscroller for Android

# How to use
1.Paste fastcroller.kt to your app/src/main/java/...

**2.Check whether the colors are well set or not.**

3.Add these code to the area where you want to use fastcroller in \layout\....xml.

(If you are not aware of where to put these codes，you can check Example files here or try some times.

```
<RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1">
            
            <androidx.recyclerview.widget.RecyclerView
                android:id="@+id/recyclerView"
                android:layout_width="match_parent"
                android:layout_height="match_parent"/>
                
            <com.example.swmu_electricity_ranking.FastScroller
                android:id="@+id/fastScroller"
                android:layout_width="40dp"
                android:layout_height="match_parent"
                android:layout_alignParentRight="true"/>
        </RelativeLayout>
```

4.Add this line to your onCreate() or  setupRecyclerView()

```
binding.fastScroller.attachToRecyclerView(binding.recyclerView)
```
