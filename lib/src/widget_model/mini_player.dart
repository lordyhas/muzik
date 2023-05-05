import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:marquee/marquee.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import 'package:on_audio_query/on_audio_query.dart';

import 'package:muzik_audio_player/src/music_player_page.dart';

import 'package:muzik_audio_player/src/widget_model/boolean_builder.dart';

class BottomPlayerView extends StatelessWidget {
  const BottomPlayerView({Key? key}) : super(key: key);

  // OnAudioQuery audioQuery = OnAudioQuery();

  TextStyle get primaryTextStyle => const TextStyle(color: Colors.white);
  @override
  Container build(BuildContext context) {

    var musicController = BlocProvider.of<PlayerControllerBloc>(context);
    var _player = BlocProvider.of<PlayerControllerBloc>(context).player;
    //SongModel song = _player.c;

    //double sw = MediaQuery.of(context).size.width;
    //MusicPlayerState status;
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
          final queue = sequenceState.sequence;

          int i = sequenceState.currentIndex;
          final media = queue[i].tag as MediaItem;

          String artist(){
            if(media.artist == null ) return "Unknown artist";
            String name = media.artist!;
            if(name.length >= 20  ) return name.substring(0,20);
            return name;
          }

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
                              int.parse(media.id),
                              ArtworkType.AUDIO,
                            ),
                          ),
                        ),
                    ),
                  ),

                  Expanded(
                    //flex: 2,
                    child: Container(
                      padding: const EdgeInsets.symmetric(vertical: 4.0, horizontal: 4.0),
                      //width: 90,
                      alignment: Alignment.centerLeft,
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.start,
                        children: <Widget>[
                          Row(
                            children: [
                              const SizedBox(width: 8.0,),
                              SizedBox(
                                height: 17,
                                //width: 200,
                                child: BooleanBuilder(
                                  check: media.title.length < 28,
                                  ifTrue: Text(
                                    media.title,
                                    maxLines: 1,
                                    style: primaryTextStyle,
                                  ),
                                  ifFalse: Expanded(
                                    //width: 190,
                                    child: Marquee(
                                      text: media.title,
                                      style: primaryTextStyle,
                                      fadingEdgeStartFraction: 0.2,
                                      fadingEdgeEndFraction: 0.2,
                                      blankSpace: 50,
                                      //onDone: (){},
                                      //numberOfRounds: 2,
                                    ),
                                  ),
                                ),
                              ),
                            ],
                          ),
                          /*Text((!snapshot.hasData)
                              ? "Music Player Song"
                              : sequence[i].tag.title,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: primaryTextStyle,
                          ),*/
                          Row(
                            children: [
                              const SizedBox(width: 8.0,),
                              Text(artist(),
                                //maxLines: 1,
                                textAlign : TextAlign.start,
                                overflow: TextOverflow.ellipsis,
                                style: const TextStyle(
                                  fontSize: 12, fontWeight: FontWeight.bold,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                  ),
                  Row(
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
