import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:just_audio/just_audio.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import 'package:on_audio_query/on_audio_query.dart';

import 'package:muzik_audio_player/src/music_player_page.dart';

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
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [
            Colors.transparent,
            Colors.transparent,
            Colors.transparent,
            Colors.transparent,
            Colors.transparent,
            Theme.of(context).backgroundColor.withOpacity(0.1),
            Theme.of(context).backgroundColor.withOpacity(0.2),
            Theme.of(context).backgroundColor.withOpacity(0.3),
            Theme.of(context).backgroundColor.withOpacity(0.4),
            Theme.of(context).backgroundColor.withOpacity(0.5),
            Theme.of(context).backgroundColor.withOpacity(0.6),
            Theme.of(context).backgroundColor.withOpacity(0.7),
            Theme.of(context).backgroundColor.withOpacity(0.8),
            Theme.of(context).backgroundColor.withOpacity(0.9),
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
            Theme.of(context).backgroundColor,
          ],
        ),
      ),
      child: StreamBuilder<SequenceState?>(
        stream: _player.sequenceStateStream,
        builder: (context, snapshot){
          //var a =snapshot.data.isEmpty;

          if (!snapshot.hasData) return const SizedBox.shrink();
          final sequenceState = snapshot.data!;
          final sequence = sequenceState.sequence;
          int i = sequenceState.currentIndex;


          //final metadata = sequenceState.currentSource!.tag as MediaItem;

          return InkWell(
            onTap: () => Navigator.push(context, MusicPlayerPage.route(),),
            child: Container(
              height: 50,
              width: MediaQuery.of(context).size.width,
              padding: const EdgeInsets.all(4.0),
              decoration: BoxDecoration(
                color: Theme.of(context).appBarTheme.backgroundColor!.withOpacity(.95),
                border: Border.all(color: Colors.teal.shade400, width: 2.0),
                borderRadius: BorderRadius.circular(30.0),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: <Widget>[
                  const SizedBox(width: 4,),
                  SizedBox.square(
                    dimension: 40,
                    //margin: const EdgeInsets.only(right: 8.0),

                    child: ClipRRect(
                        borderRadius: BorderRadius.circular(100.0),
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
