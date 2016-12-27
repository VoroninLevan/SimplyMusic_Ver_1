package comvoroninlevan.httpsgithub.symplymusic;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Levan on 06.11.2016.
 */

public class SongsAdapter extends ArrayAdapter<Songs> {

    public SongsAdapter(Context context, ArrayList<Songs> songsArrayList){
        super(context, 0, songsArrayList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItem = convertView;
        if(listItem == null){
            listItem = LayoutInflater.from(getContext()).inflate(R.layout.song_item, parent, false);
        }

        Songs currentSong = getItem(position);

        TextView title = (TextView)listItem.findViewById(R.id.title);
        title.setText(currentSong.getSongTitle());

        TextView artist = (TextView)listItem.findViewById(R.id.artist);
        artist.setText(currentSong.getSongArtist());

        return listItem;
    }

}
