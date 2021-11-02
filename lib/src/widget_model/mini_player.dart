import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:get/get.dart';
import 'package:just_audio/just_audio.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import 'package:on_audio_query/on_audio_query.dart';

import '../music_player_page.dart';

class BottomPlayerView extends StatelessWidget {
  const BottomPlayerView({Key? key}) : super(key: key);

  // OnAudioQuery audioQuery = OnAudioQuery();

  TextStyle get primaryTextStyle => const TextStyle(color: Colors.white);
  @override
  Container build(BuildContext context) {

    var musicController = BlocProvider.of<PlayerControllerBloc>(context);
    var _player = BlocProvider.of<PlayerControllerBloc>(context).player;
    //SongModel song = _player.c;

    Color iconColor1 = Colors.white;

    double iSizeNB = 30;
    double iSizeP = 30;

    double sw = MediaQuery.of(context).size.width;
    //MusicPlayerState status;
    String imagePath;
    return Container(
      //color: Colors.transparent,
      child: StreamBuilder<SequenceState?>(
        stream: _player.sequenceStateStream,
        builder: (context, snapshot){
          //var a =snapshot.data.isEmpty;

          if (!snapshot.hasData) return Container();
          final sequenceState = snapshot.data!;
          final sequence = sequenceState.sequence;
          int i = sequenceState.currentIndex;


          //final metadata = sequenceState.currentSource!.tag as MediaItem;

          return InkWell(
            onTap: () => Get.to(() => const MusicPlayerPage()),
            child: Container(
              height: 65,
              width: MediaQuery.of(context).size.width,
              padding: const EdgeInsets.all(8.0),
              decoration: BoxDecoration(
                color: Theme.of(context).appBarTheme.backgroundColor!.withOpacity(.95),
                border: Border.all(color: Colors.blue, width: 2.0),
                borderRadius: BorderRadius.circular(15.0),

              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  Container(
                    margin: const EdgeInsets.only(right: 8.0),
                    child: ClipRRect(
                        borderRadius: const BorderRadius.vertical(
                          top: Radius.circular(50.0),
                          bottom: Radius.circular(50.0),
                        ),
                        child: SizedBox(
                          //width: 50,
                          child: GetImageCoverItem(
                            //fit: BoxFit.fitWidth,
                            futureResource: OnAudioQuery().queryArtwork(
                              int.parse(sequence[i].tag.id),
                              ArtworkType.AUDIO,
                            ),
                          ),
                        )),
                  ),

                  Expanded(
                    //flex: 2,
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 5.0),
                      //width: 90,
                      child: Column(
                        children: <Widget>[
                          Text((!snapshot.hasData)
                              ? "Music Player Song"
                              : sequence[i].tag.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: primaryTextStyle,),
                          Text((!snapshot.hasData) ?"Unknown artist": "by ${sequence[i].tag.artist}",
                            maxLines: 1,style: const TextStyle(
                                fontSize: 12, fontWeight: FontWeight.bold),),
                        ],
                      ),
                    ),
                  ),
                  Container(
                    //width: sw * 0.467,
                    child: Row(
                      children: <Widget>[
                        StreamBuilder<bool>(
                          stream: _player.playingStream,
                          builder: (context, playingSnap) {
                            if(!playingSnap.hasData) {
                              return IconButton(
                                icon: (!_player.playing)
                                    ? const Icon(CupertinoIcons.play_arrow )
                                    : const Icon(CupertinoIcons.pause),
                                color: Colors.white,
                                onPressed: () {},
                              );
                            }
                            return IconButton(
                              icon: (!playingSnap.data!)
                                  ? const Icon(CupertinoIcons.play_arrow )
                                  : const Icon(CupertinoIcons.pause),
                              color: Colors.white,
                              onPressed: () {
                                if(playingSnap.data!) {
                                  musicController.pause();
                                } else {
                                  musicController.play();
                                }

                              },
                            );
                          }
                        ),

                        IconButton(
                          icon: const Icon(CupertinoIcons.forward_end,),
                          color: Colors.white,
                          //onPressed: forward,
                          //iconSize: iSizeNB,
                          onPressed: () => musicController.next() ,
                        ),
                      ],
                    ),
                  ),
                ],
              ),
              /*ListView(
              children: <Widget>[
                ListTile(title: Text('More', style: primaryTextStyle24,),
                  trailing: IconButton(
                    icon: Icon(Icons.close,color: iconWhiteColor,),
                    onPressed: () => Navigator.pop(context),
                  ),),
              ],
            ),*/
            ),
          );
        },
      ),
    );
  }
}
