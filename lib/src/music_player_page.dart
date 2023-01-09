import 'dart:math';
import 'dart:typed_data';
import 'dart:ui';


import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:marquee/marquee.dart';
import 'package:muzik_audio_player/data/app_bloc/app_bloc.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_playlist_type.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:muzik_audio_player/data/values.dart';
import 'package:muzik_audio_player/src/widget_model/boolean_builder.dart';
import 'package:muzik_audio_player/src/widget_model/dialog_widget.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:muzik_audio_player/src/layout/details_content_screen.dart';
import 'package:page_transition/page_transition.dart';

part 'player_ui/player_controllers_ui.dart';

part 'player_ui/image_cover_widget.dart';

class MusicPlayerPage extends StatefulWidget {
  const MusicPlayerPage({Key? key}) : super(key: key);

  static PageRouteBuilder route() =>
      PageTransition(
        type: PageTransitionType.bottomToTop,
        child: const MusicPlayerPage(),
      );


  @override
  State<MusicPlayerPage> createState() => _MusicPlayerPageState();
}

class _MusicPlayerPageState extends State<MusicPlayerPage> {
  //final ScrollController _scrollController = ScrollController();

  bool isMute = false, isShuffle = false, isRepeat = false;

  @override
  void initState() {
    super.initState();
  }

  String fromDuration(Duration duration) {
    return (duration
        .toString()
        .split('.')
        .first).substring(2);
  }

  @override
  void dispose() {
    super.dispose();
  }

  final TextStyle primaryTextStyle20sp = const TextStyle(
      color: Colors.white,
      fontSize: 20,
      fontFamily: "ubuntu"
  );

  var iconWhiteColor = Colors.white;

  @override
  Widget build(BuildContext context) {
    setSystemUiOverlayStyle();
    AudioPlayer _player = BlocProvider
        .of<PlayerControllerBloc>(context)
        .player;

    double iSize2 = 27;
    TextStyle titleStyle =
    const TextStyle(color: Colors.white, fontFamily: "ubuntu");
    return BlocBuilder<SettingCubit, SettingState>(
      builder: (context, setting) {
        return Scaffold(
          //primary: false,
          extendBodyBehindAppBar: true,
          backgroundColor: Colors.grey.shade800,
          //appBar:
          body: Stack(
            children: [
              Stack(
                children: [
                  if(BlocProvider.of<SettingCubit>(context).state.coverBehind)
                    SizedBox(
                      child: StreamBuilder<SequenceState?>(
                        stream: _player.sequenceStateStream,
                        builder: (context, snapshot) {
                          final state = snapshot.data;
                          if (state?.sequence.isEmpty ?? true) {
                            return const SizedBox();
                          }
                          final metadata = state?.currentSource!
                              .tag as MediaItem;
                          return GetImageCoverBackground(
                            fit: BoxFit.cover,
                            futureResource: OnAudioQuery().queryArtwork(
                              int.parse(metadata.id),
                              ArtworkType.AUDIO,

                            ),
                          );
                        },
                      ),
                    ),
                  if(BlocProvider.of<SettingCubit>(context).state.coverBehind)
                    BackdropFilter(
                      filter: ImageFilter.blur(
                        sigmaX: 12.0,
                        sigmaY: 8.0,

                      ),
                      child: Container(
                        color: Theme
                            .of(context)
                            .primaryColor
                            .withOpacity(0.3),
                      ),
                    ),
                  if(BlocProvider.of<SettingCubit>(context).state.coverBehind)
                    Container(
                      color: Colors.black.withOpacity(0.3),
                    ),

                  Column(
                    crossAxisAlignment: CrossAxisAlignment.center,
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [

                      /// The AppBar
                      /*SizedBox(
                        height: AppBar().preferredSize.height,
                      ),*/
                      AppBar(
                        elevation: 0.0,
                        backgroundColor: Colors.transparent,
                        leading: IconButton(
                          onPressed: Navigator.of(context).pop,
                          icon: const Icon(CupertinoIcons.xmark),
                        ),
                        centerTitle: true,
                        title: const Text(
                          "",
                          style: TextStyle(fontSize: 24, color: Colors.white),
                        ),

                        /**
                         * FontAwesomeIcons.volumeDown
                         * FontAwesomeIcons.volumeUp .volumeOff
                         * FontAwesomeIcons.chevronDown
                         * */
                        actions: <Widget>[
                          //BlocBuilder<PlayerController,>(builder: builder)
                          IconButton(
                            icon: (isMute)
                                ? const Icon(CupertinoIcons.volume_off)
                                : const Icon(CupertinoIcons.volume_up), //Icon(Icons.volume_of),
                            color: iconWhiteColor,
                            onPressed: () {
                              ShowOver.of(context).sliderVolume(
                                title: "Adjust volume",
                                divisions: 10,
                                min: 0.0,
                                max: 1.0,
                                stream: _player.volumeStream,
                                onChanged: _player.setVolume,
                              );
                            },
                            iconSize: iSize2,
                          ),
                          StreamBuilder<List<dynamic>>(
                              stream: OnAudioQuery().queryWithFilters(
                                context.read<PlayerControllerBloc>().currentSong.artist,
                                WithFiltersType.ARTISTS,
                                //args: ArtistsArgs.ARTIST,
                              ).asStream(),
                              builder: (context, snapshot) {

                                return PopupMenuButton(
                                  color: Theme.of(context).backgroundColor.withOpacity(0.9),
                                  icon: const Icon(CupertinoIcons.ellipsis_vertical,),
                                  //onSelected: (index) => Navigator.of(context).pop(),
                                  itemBuilder: (BuildContext context) =>
                                      <Widget>[
                                        ListTile(
                                          leading: Icon(
                                              CupertinoIcons.arrowshape_turn_up_right,
                                              color: iconWhiteColor),
                                          title: Text('Share', style: primaryTextStyle20sp),
                                          onTap: _defaultOnTap,
                                        ),
                                        ListTile(
                                          leading: Icon(CupertinoIcons.text_badge_plus,
                                              color: iconWhiteColor),
                                          title: Text('Add to playlist',
                                            style: primaryTextStyle20sp,
                                          ),
                                          onTap: null, //() => _addToPlaylist(),
                                        ),
                                        ListTile(
                                          leading: Icon(
                                            CupertinoIcons.clock,
                                            color: iconWhiteColor,
                                          ),
                                          title: Text('Delay', style: primaryTextStyle20sp),
                                          onTap: () {
                                            Navigator.pop(context);
                                          },
                                        ),
                                        if (snapshot.hasData)
                                          ListTile(
                                            leading:
                                            Icon(CupertinoIcons.person_crop_rectangle,
                                                color: iconWhiteColor),
                                            title: Text('See artist',
                                              style: primaryTextStyle20sp,
                                            ),
                                            onTap: (){
                                              var artists = snapshot.data!.toArtistModel();
                                              Navigator.push(context,
                                                SingleArtistScreen.route(
                                                  artist: artists.first,
                                                ),
                                              );
                                            }, //() => _openArtistPage(actualMusic),
                                          ),
                                        ListTile(
                                          leading: Icon(CupertinoIcons.collections,
                                            color: iconWhiteColor,
                                          ),
                                          title: Text('Cover Behind : ${BlocProvider
                                              .of<SettingCubit>(context)
                                              .state
                                              .coverBehind ? "On" : "Off"} ',
                                            style: primaryTextStyle20sp,
                                          ),
                                          onTap: () {
                                            //_player.stop();
                                            BlocProvider.of<SettingCubit>(context).save(
                                                setting..coverBehind = !setting.coverBehind);
                                            Navigator.pop(context);
                                          },
                                        ),
                                        ListTile(
                                          leading:
                                          Icon(CupertinoIcons.slider_horizontal_3,
                                            color: iconWhiteColor,
                                          ),
                                          title: Text(
                                            'Equalizer',
                                            style: primaryTextStyle20sp,
                                          ),
                                          onTap: _defaultOnTap,
                                        ),
                                        ListTile(
                                            leading: Icon(CupertinoIcons.info_circle_fill,
                                              color: iconWhiteColor,
                                            ),
                                            title: Text(
                                              'Song Details',
                                              style: primaryTextStyle20sp,
                                            ),
                                            onTap: () {
                                              Navigator.pop(context);
                                              ShowOver.musicInfo(context,
                                                song: context.read<PlayerControllerBloc>()
                                                    .currentSong,
                                              );

                                            }
                                        ),
                                      ].map((item) => PopupMenuItem(child: item)).toList(),
                                );
                              }
                          ),
                        ],
                      ),
                      //const SizedBox(height: 10,),
                      const Spacer(),

                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 8.0),
                        child: StreamBuilder<SequenceState?>(
                          stream: _player.sequenceStateStream,
                          builder: (context, snapshot) {
                            final state = snapshot.data;
                            if (state?.sequence.isEmpty ?? true) {
                              return const SizedBox();
                            }
                            final MediaItem metadata = state?.currentSource!.tag as MediaItem;
                            return Column(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                Center(
                                  child: ConstrainedBox(
                                    constraints: const BoxConstraints(
                                      minHeight: 280,
                                      minWidth: 280,
                                      maxHeight: 300,
                                      maxWidth: 300,
                                    ),
                                    child: ClipRRect(
                                      borderRadius: const BorderRadius.vertical(
                                        top: Radius.circular(20.0),
                                        bottom: Radius.circular(20.0),
                                      ),
                                      child: GetImageCover(
                                        futureResource:
                                        OnAudioQuery().queryArtwork(
                                          int.parse(metadata.id),
                                          ArtworkType.AUDIO,
                                          size: 500,
                                        ),
                                      ),
                                    ),
                                  ),
                                ),
                                Center(
                                  child: Container(
                                    margin: const EdgeInsets.symmetric(
                                        horizontal: 42.0, vertical: 16.0),
                                    child: Column(
                                      mainAxisAlignment: MainAxisAlignment
                                          .center,
                                      children: [
                                        SizedBox(
                                          height: 30,
                                          child: BooleanBuilder(
                                            check: metadata.title.length < 28,
                                            ifTrue: Text(
                                              metadata.title,
                                              style: titleStyle.copyWith(
                                                  fontSize: 20,
                                                  fontWeight: FontWeight.bold),
                                              maxLines: 1,
                                              overflow: TextOverflow.ellipsis,
                                            ),
                                            ifFalse: Marquee(
                                              text: metadata.title,
                                              style: titleStyle.copyWith(
                                                  fontSize: 20,
                                                  fontWeight: FontWeight.bold),
                                              fadingEdgeStartFraction: 0.2,
                                              fadingEdgeEndFraction: 0.2,
                                              blankSpace: 50,
                                              //onDone: (){},
                                              //numberOfRounds: 2,
                                            ),
                                          ),
                                        ),
                                        /*Text(
                                      metadata.title,
                                      style: titleStyle.copyWith(
                                          fontSize: 20,
                                          fontWeight: FontWeight.bold),
                                      maxLines: 1,
                                      overflow: TextOverflow.ellipsis,
                                    ),*/
                                        const SizedBox(height: 4.0,),
                                        Text(
                                          metadata.artist ?? "Unknown",
                                          style: titleStyle.copyWith(
                                              color: Colors.white54),
                                          maxLines: 1,
                                          overflow: TextOverflow.ellipsis,
                                        ),
                                      ],
                                    ),
                                  ),
                                ),
                              ],
                            );
                          },
                        ),
                      ),
                      const Spacer(),
                      const SizedBox(height: 8.0),
                      Container(
                        height: 35,
                        width: MediaQuery
                            .of(context)
                            .size
                            .width * 0.50,
                        padding: const EdgeInsets.symmetric(horizontal: 16.01),
                        decoration: BoxDecoration(
                          //shape: BoxShape.circle, //const CircleBorder(),
                          color: Theme
                              .of(context)
                              .scaffoldBackgroundColor
                              .withOpacity(0.7),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.spaceBetween,
                          children: [
                            IconButton(
                              tooltip: 'lyrics',
                              onPressed: () {},
                              icon: const Icon(
                                CupertinoIcons.text_quote,
                                size: 16,
                              ),
                            ),
                            IconButton(
                              tooltip: 'heart',
                              onPressed: () {},
                              icon: const Icon(
                                CupertinoIcons.heart,
                                size: 16,
                              ),
                            ),
                            IconButton(
                              tooltip: 'timer',
                              onPressed: () {},
                              icon: const Icon(
                                CupertinoIcons.clock,
                                size: 16,
                              ),
                            ),
                          ],
                        ),
                      ),

                      StreamBuilder<Duration?>(
                        stream: _player.durationStream,
                        builder: (context, snapshot) {
                          final duration = snapshot.data ?? Duration.zero;
                          return StreamBuilder<Duration>(
                            stream: _player.positionStream,
                            builder: (context, snapshot) {
                              var position = snapshot.data ?? Duration.zero;
                              if (position > duration) {
                                position = duration;
                              }
                              return Container(
                                margin:
                                const EdgeInsets.symmetric(horizontal: 16.0),
                                child: SeekBar(
                                  duration: duration,
                                  position: position,
                                  onChangeEnd: (newPosition) {
                                    _player.seek(newPosition);
                                  },
                                ),
                              );
                            },
                          );
                        },
                      ),

                      const SizedBox(height: 8.0),
                      const ControlButtons(),
                      //const SizedBox(height: 8.0),
                      const Spacer(),
                      const SizedBox(height: 60.0,),
                    ],
                  ),
                ],
              ),
              QueueBottomSheetUI(
                color: Theme.of(context).scaffoldBackgroundColor,
                child: const QueueSongList(),
              ),
            ],
          ),
        );
      },
    );
  }

  void _defaultOnTap() {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(const SnackBar(
          behavior: SnackBarBehavior.floating,
          backgroundColor: Colors.white70,
          content: Text('En développment | Soon :) ')));
  }
}


class QueueSongList extends StatefulWidget {
  const QueueSongList({Key? key}) : super(key: key);

  @override
  State<QueueSongList> createState() => _QueueSongListState();
}

class _QueueSongListState extends State<QueueSongList> {
  String fromDuration(Duration duration) {
    return (duration
        .toString()
        .split('.')
        .first).substring(2);
  }

  String artistName(SongInfo song) {
    if (song.artist.length <= 20) {
      return song.artist;
    } else {
      return song.artist.substring(0, 20) + "...";
    }
  }

  @override
  Widget build(BuildContext context) {
    //AudioPlayer _player = BlocProvider.of<PlayerController>(context).player;
    bool ignorePointer = false;

    //late ScrollController scrollController;

    return BlocBuilder<PlayerControllerBloc, PlayerControllerState>(
      builder: (context, state) {
        final currentIndex = state.songIndex;
        final Playlist queue = state.currentPlaylist;

        /*scrollController = ScrollController(
            initialScrollOffset: currentIndex.toDouble()*73);*/

        if (queue.isEmpty) return Container();
        //queue.removeRange(0, currentIndex);
        return Expanded(
          //margin:const EdgeInsets.only(top: 32.0),
          //height: MediaQuery.of(context).size.height,
          //padding: const EdgeInsets.all(8.0),

          child: Column(
            children: [
              Expanded(
                child: IgnorePointer(
                  ignoring: ignorePointer,
                  child: ReorderableListView(
                    scrollController: ScrollController(
                        initialScrollOffset: currentIndex.toDouble() * 73),
                    onReorder: (int oldIndex, int newIndex) {
                      if (oldIndex < newIndex) newIndex--;


                      //sequence.
                      //sequence.toAlbumModel()
                    },
                    children: [
                      for (var i = 0; i < queue.length; i++)
                        Dismissible(
                          key: ValueKey(queue[i]),
                          background: Container(
                            color: Colors.redAccent,
                            alignment: Alignment.centerRight,
                            child: const Padding(
                              padding: EdgeInsets.only(right: 8.0),
                              child: Icon(Icons.delete, color: Colors.white),
                            ),
                          ),
                          onDismissed: (dismissDirection) {
                            BlocProvider
                                .of<PlayerControllerBloc>(
                                context).removeAt(index: i);

                            setState(() {});
                            //sequence.removeAt(i);
                          },
                          child: Container(
                            color: i == currentIndex
                                ?Theme.of(context).primaryColor.withOpacity(0.5)
                                :null,
                            margin: const EdgeInsets.symmetric(vertical: 0.0),
                            child: Column(
                              children: [
                                Padding(
                                  padding:
                                  const EdgeInsets.symmetric(horizontal: 4.0),
                                  child: Row(
                                    children: [
                                      SizedBox(
                                        child: Text('${i + 1}'),
                                      ),
                                      Expanded(
                                        child: ListTile(
                                          onTap: () {
                                            BlocProvider
                                                .of<PlayerControllerBloc>(
                                                context).playAt(index: i);
                                            //state.player.seek(Duration.zero,index: i);
                                            setState(() {});
                                          },
                                          //minLeadingWidth: 0.0,
                                          ///horizontalTitleGap: 4.0, // todo: uncomment when ready
                                          ///minVerticalPadding: 0.0, // todo: uncomment when ready
                                          //tileColor: Colors.grey,
                                          leading: ClipRRect(
                                            borderRadius: BorderRadius.circular(
                                                5),
                                            child: SizedBox.square(
                                              dimension: 40,
                                              child: GetImageCoverItem(
                                                //fit: BoxFit.fitWidth,
                                                futureResource:
                                                OnAudioQuery().queryArtwork(
                                                  queue[i].id,
                                                  ArtworkType.AUDIO,
                                                  //size: 300,
                                                ),
                                              ),
                                            ),
                                          ),
                                          title: Text(
                                            queue[i].title,
                                            overflow: TextOverflow.ellipsis,
                                          ),
                                          subtitle: Text(artistName(queue[i])+"", //+ ' ● ' + fromDuration(queue[i].duration),
                                            //Duration(milliseconds: int.parse(sequence[i].duration!.inMinutes.toString()))
                                            style: const TextStyle(fontSize:12),
                                          ),
                                          trailing: (i == currentIndex)
                                              ? const Icon(Icons.play_arrow)
                                              : null,
                                          //trailing: IconButton(icon: Icon(Icons.more_vert), onPressed: () {}),
                                        ),
                                      ),
                                      InkWell(
                                        child: const Icon(Icons.more_vert),
                                        onTap: () {},
                                      ),
                                    ],
                                  ),
                                ),
                                Container(
                                  color: Colors.white60,
                                  height: 1,
                                  margin: const EdgeInsets.symmetric(
                                    horizontal: 24,
                                  ),
                                )
                              ],
                            ),
                          ),
                        ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }
}



