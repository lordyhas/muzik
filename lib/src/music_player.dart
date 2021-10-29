import 'dart:math';
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';
import 'package:get/get.dart';
import 'package:just_audio/just_audio.dart';
import 'package:just_audio_background/just_audio_background.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';

import 'package:muzik_audio_player/src/player_ui/current_list.dart';
import 'package:on_audio_query/on_audio_query.dart';

part 'player_ui/player_controllers_ui.dart';
part 'player_ui/image_cover_widget.dart';

class MusicPlayerPage extends StatefulWidget {
  const MusicPlayerPage({Key? key}) : super(key: key);


  @override
  _MusicPlayerPageState createState() => _MusicPlayerPageState();
}

class _MusicPlayerPageState extends State<MusicPlayerPage> {
  final ScrollController _scrollController = ScrollController();
  //ConcatenatingAudioSource _songPlaylist;

  //String status = 'hidden';
  bool isMute = false, isShuffle = false, isRepeat = false;

  @override
  void initState() {
    super.initState();
    //BlocProvider.of<PlayerController>(context).player.
    //AudioServiceBackground.setMediaItem(mediaItem)

    //BlocProvider.of<PlayerController>(context).player.set

    /* _songPlaylist = ConcatenatingAudioSource(
        children: songList.map(
                (song) => AudioSource.uri(
                Uri.file(song.filePath),
                tag: song)).toList()
    );*/
  }

  String fromDuration(Duration duration) {
    return (duration.toString().split('.').first).substring(2);
  }

  @override
  void dispose() {
    //_player.dispose();
    super.dispose();
  }

  final TextStyle primaryTextStyle20sp =
      const TextStyle(color: Colors.white, fontSize: 20, fontFamily: "ubuntu");
  var iconWhiteColor = Colors.white;

  @override
  Widget build(BuildContext context) {
    AudioPlayer _player = BlocProvider.of<PlayerController>(context).player;

    double iSize2 = 27;
    TextStyle titleStyle =
        const TextStyle(color: Colors.white, fontFamily: "ubuntu");
    return Scaffold(
      //primary: false,
      backgroundColor: Colors.grey.shade800,
      body: Stack(
        children: [
          SafeArea(
            child: Stack(
              children: [
                /*Container(
                  child: StreamBuilder<SequenceState?>(
                    stream: _player.sequenceStateStream,
                    builder: (context, snapshot) {
                      final state = snapshot.data;
                      if (state?.sequence.isEmpty ?? true) return const SizedBox();
                      final metadata = state?.currentSource!.tag as MediaItem;
                      return GetImageCoverBackground(
                        futureResource: OnAudioQuery().queryArtwork(
                          int.parse(metadata.id),
                          ArtworkType.AUDIO,

                        ),
                      );
                    },
                  ),
                ),
                BackdropFilter(
                  filter: ImageFilter.blur(
                    sigmaX: 12.0,
                    sigmaY: 8.0,

                  ),
                  child: Container(
                    color: Theme.of(context).primaryColor.withOpacity(0.3),
                  ),
                ),
                Container(
                  color: Colors.black.withOpacity(0.3),
                ),*/

                Column(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    /// The AppBar
                    AppBar(
                      elevation: 0.0,
                      backgroundColor: Colors.transparent,
                      leading: IconButton(
                        onPressed: Get.back,
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
                              : const Icon(CupertinoIcons
                                  .volume_up), //Icon(Icons.volume_of),
                          color: iconWhiteColor,
                          onPressed: () {
                            _showSliderDialog(
                              context: context,
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
                        PopupMenuButton(
                          color: Theme.of(context)
                              .backgroundColor
                              .withOpacity(0.9),
                          icon: const Icon(
                            CupertinoIcons.ellipsis_vertical,
                          ),
                          onSelected: (index) => Navigator.of(context).pop(),
                          itemBuilder: (BuildContext context) => _menuItems()
                              .map((menuItem) => PopupMenuItem(child: menuItem))
                              .toList(),
                        ),
                        //MyPopupMenuButton(),
                        /*IconButton(
                              icon: Icon(FontAwesomeIcons.ellipsisV, size: 20, color: textWhiteColor,),
                              color: iconWhiteColor,
                              onPressed: () => showModalBottomSheet(
                                  context: context,
                                  builder: (context) => _buildMenuBottomSheet(context),
                              ),

                              //() => this._scaffoldKey.currentState.showBottomSheet((context) => _buildBottomSheet(context)),
                              iconSize: iSize2,
                            ),*/
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
                          final metadata =
                              state?.currentSource!.tag as MediaItem;
                          return Column(
                            crossAxisAlignment: CrossAxisAlignment.center,
                            children: [
                              Center(
                                child: ConstrainedBox(
                                  constraints: const BoxConstraints(
                                    minHeight: 300,
                                    minWidth: 300,
                                    maxHeight: 350,
                                    maxWidth: 320,
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
                                      ),
                                    ), /*(metadata.albumArtwork != null)
                                      GetImageCover(
                                      futureResource: playerBloc.audioQuery
                                          .getArtwork(type: ResourceType.SONG, id: metadata.id),
                                    ), */ /*(metadata.albumArtwork != null)
                                          ?Image.file(File(metadata.albumArtwork),
                                        fit: BoxFit.cover,
                                        gaplessPlayback: false,
                                      )
                                          : Image.asset("assets/no_cover4.png",
                                        fit: BoxFit.cover,
                                        gaplessPlayback: false,
                                      ),*/
                                  ),
                                ),
                              ),
                              Center(
                                child: Container(
                                  margin: const EdgeInsets.symmetric(
                                      horizontal: 42.0, vertical: 16.0),
                                  child: Column(
                                    mainAxisAlignment: MainAxisAlignment.center,
                                    children: [
                                      /// todo: defilement du text
                                      Text(
                                        metadata.title,
                                        style: titleStyle.copyWith(
                                            fontSize: 20,
                                            fontWeight: FontWeight.bold),
                                        maxLines: 1,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      const SizedBox(
                                        height: 4.0,
                                      ),
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
                      width: MediaQuery.of(context).size.width * 0.50,
                      padding: const EdgeInsets.symmetric(horizontal: 16.01),
                      decoration: BoxDecoration(
                        //shape: BoxShape.circle, //const CircleBorder(),
                        color: Theme.of(context)
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
                    const SizedBox(
                      height: 60.0,
                    )

                    /*ClipRRect(
                      borderRadius: const BorderRadius.vertical(
                          top: Radius.circular(20),
                      ),
                      child: Container(
                        color: Theme.of(context).scaffoldBackgroundColor,
                        height: 50.0,
                        child: StreamBuilder<SequenceState?>(
                          stream: _player.sequenceStateStream,
                          builder: (context, snapshot) {
                            final state = snapshot.data;
                            final sequence = state?.sequence ?? [];
                            return Container( /// ---- Playlist and Add icon ----
                              margin: const EdgeInsets.only(left: 10.0, right: 10.0),
                              child: Row(
                                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                                children: <Widget>[
                                  IconButton(
                                    icon: Icon(Icons.playlist_play_outlined, size: 30, color: iconWhiteColor),
                                    onPressed: sequence.isEmpty ?
                                    null : () {
                                      /*showModalBottomSheet(
                                          isScrollControlled: true,
                                          enableDrag: true,
                                          context: context,
                                          // _buildPlaylistBottomSheet(context),
                                          builder: (context){
                                            if(!snapshot.hasData) {
                                              return const CircularProgressIndicator();
                                            }
                                            return _buildPlaylist(context, snapshot.data!);
                                          }

                                      );*/
                                      /*if(snapshot.hasData) {
                                        Navigator.push(context, MaterialPageRoute(builder: (context) =>
                                            Scaffold(
                                              body: _buildPlaylist(context, snapshot.data!),
                                            )
                                        ));
                                      }*/
                                    },
                                    iconSize: 20,
                                  ),

                                  Container(
                                    height: 5,
                                    width: 50,
                                    decoration: BoxDecoration(
                                      //shape: BoxShape.circle, //const CircleBorder(),
                                      color: Colors.grey,
                                      borderRadius: BorderRadius.circular(20),
                                    ),
                                  ),

                                  IconButton(
                                    icon: const Icon(Icons.library_add),
                                    color: iconWhiteColor,
                                    onPressed: null, //() => _addToPlaylist(),
                                    iconSize: 20,
                                  ),
                                ],
                              ),
                            );
                          },
                        ),
                      ),
                    ),*/
                  ],
                ),
              ],
            ),
          ),
          CurrentListBottomSheet(
            color: Theme.of(context).scaffoldBackgroundColor,
            child: StreamBuilder<SequenceState?>(
              stream: _player.sequenceStateStream,
              builder: (context, snapshot) {
                if (!snapshot.hasData) return Container();
                final sequenceState = snapshot.data!;
                //final sequence = state?.sequence ?? [];
                return QueueSongList(sequenceState,);
              },
            ),
          ),
        ],
      ),
      /*floatingActionButton: FloatingActionButton(
          child: Icon(Icons.add),
          onPressed: () {
            _songPlaylist.add(AudioSource.uri(
              Uri.parse("asset:///audio/nature.mp3"),
              tag: AudioMetadata(
                album: "Public Domain",
                title: "Nature Sounds ${++_addedCount}",
                artwork:
                "https://media.wnyc.org/i/1400/1400/l/80/1/ScienceFriday_WNYCStudios_1400.jpg",
              ),
            ));
          },
        ),*/
    );
  }

  void _defaultOnTap() {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(const SnackBar(
          behavior: SnackBarBehavior.floating,
          content: Text('En développment | Soon :) ')));
  }



  /*
  Container? _buildPlaylistBottomSheet(
      BuildContext context,
      SequenceState sequenceState,
      [ScrollController? scrollController]) {
    final state = sequenceState;
    final sequence = state.sequence ?? [];
    if(sequence.isEmpty) return null;
    return Container(
      margin:const EdgeInsets.only(top: 32.0),
      height: MediaQuery.of(context).size.height,
      padding: const EdgeInsets.all(8.0),
      decoration: BoxDecoration(
        color: background.withOpacity(0.95),
        //border: Border.all(color: Colors.blue, width: 2.0),
        /*border: Border(
          top: BorderSide(color: Colors.blue, width: 2.0),
          left: BorderSide(color: Colors.blue, width: 2.0),
          right: BorderSide(color: Colors.blue, width: 2.0),
          bottom: BorderSide(color: Colors.red, width: 2.0),
      ),*/
        //borderRadius: BorderRadius.circular(15.0),
        borderRadius: BorderRadius.only(
            topLeft: Radius.circular(15.0),
            topRight: Radius.circular(15.0)),

      ),
      child: Column(
        children: [
          Container(
            //height: 50,

              child: ClipRRect(
                //borderRadius: BorderRadius.circular(15),
                borderRadius: BorderRadius.only(
                    topLeft: Radius.circular(15.0),
                    topRight: Radius.circular(15.0)),
                child: AppBar(
                  //automaticallyImplyLeading: false,
                  backgroundColor: Theme.of(context).primaryColorDark.withOpacity(0.2),
                  centerTitle: true,
                  leading: IconButton(
                    icon: Icon(Icons.keyboard_arrow_down),
                    onPressed: Navigator.of(context).pop,
                  ),
                  title: Text("Playlist ● En attente"),
                ),
              )  /*Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Text("Playlist"),
              ],
            ),*/
          ),

          Container(
            color: Colors.grey,
            height: 1,
          ),

          Expanded(
            child: Container(
              //color: Colors.transparent,
              child: ReorderableListView(
                scrollController: scrollController,
                onReorder: (int oldIndex, int newIndex) {
                  if (oldIndex < newIndex) newIndex--;
                  _songPlaylist.move(oldIndex, newIndex);
                },
                children: [
                  for (var i = 0; i < sequence.length; i++)
                    Dismissible(
                      key: ValueKey(sequence[i]),
                      background: Container(
                        color: Colors.redAccent,
                        alignment: Alignment.centerRight,
                        child: Padding(
                          padding: const EdgeInsets.only(right: 8.0),
                          child: Icon(Icons.delete, color: Colors.white),
                        ),
                      ),
                      onDismissed: (dismissDirection) {
                        _songPlaylist.removeAt(i);
                      },
                      child: Container(
                        color: i == state.currentIndex
                            ? Theme.of(context).accentColor.withOpacity(0.5)
                            : null,
                        margin: EdgeInsets.symmetric(vertical: 0.0),
                        child: Column(
                          children: [
                            Padding(
                              padding: const EdgeInsets.symmetric(horizontal: 4.0),
                              child: Row(
                                children: [
                                  Container(
                                    child: Text('${i + 1}'),
                                  ),
                                  Expanded(
                                    child: Container(
                                      child: ListTile(
                                        onTap: () {
                                          _player.seek(Duration.zero,index: i);
                                        },
                                        //minLeadingWidth: 0.0,
                                        ///horizontalTitleGap: 4.0, // todo: uncomment when ready
                                        ///minVerticalPadding: 0.0, // todo: uncomment when ready
                                        //tileColor: Colors.grey,
                                        leading: Container(
                                          child: ClipRRect(
                                            borderRadius: BorderRadius.circular(5),
                                            child: Container(
                                              height: 40,
                                              width: 40,
                                              child: GetImageCover(
                                                futureResource: playerBloc.audioQuery
                                                    .getArtwork(
                                                    type: ResourceType.SONG, id: sequence[i].tag.id),
                                              ),
                                            ),
                                          ),
                                        ),
                                        title: Container(
                                          ///color: Colors.blue,
                                          child: Text(
                                            sequence[i].tag.title,
                                            overflow: TextOverflow.ellipsis,
                                          ),
                                        ),
                                        subtitle: Text('${ (
                                            (sequence[i].tag.artist.length <= 10)
                                                ?sequence[i].tag.artist
                                                : sequence[i].tag.artist.substring(0,10)+"...")
                                            ?? "Unknown Artist"}' +
                                            ' ● ' +
                                            fromDuration(Duration(milliseconds: int.parse(sequence[i].tag.duration))),
                                          style: TextStyle(fontSize: 12),
                                        ),
                                        //trailing: IconButton(icon: Icon(Icons.more_vert), onPressed: () {}),
                                      ),
                                    ),
                                  ),
                                  Container(
                                    child: InkWell(
                                      child:  Icon(Icons.more_vert),
                                      onTap: (){},
                                    ), /*IconButton(
                                      onPressed: (){},
                                      icon: Icon(Icons.more_vert),
                                      ///icon:Icon(Icons.drag_handle_rounded),
                                    ),*/
                                  ),
                                ],
                              ),
                            ),
                            Container(
                              color: Colors.white60,
                              height: 1,
                              margin: EdgeInsets.symmetric(horizontal: 24),
                            )
                            /*Divider(
                                  color: Colors.white60,
                                  indent: 24.0,
                                  endIndent: 32.0,
                                ),*/
                          ],
                        ),
                      ),
                    ),
                ],
              ),
              /*StreamBuilder<SequenceState>(
                initialData: sequenceState,
                stream: _player.sequenceStateStream,
                builder: (context, snapshot) {
                  final state = snapshot.data;
                  final sequence = state?.sequence ?? [];
                  return null;
                },
              ),*/
            ),
          ),
        ],
      ),
    );
  }*/

  List<Widget> _menuItems() => <Widget>[
        ListTile(
          leading: Icon(CupertinoIcons.arrowshape_turn_up_right,
              color: iconWhiteColor),
          title: Text('Share', style: primaryTextStyle20sp),
          onTap: _defaultOnTap,
        ),
        ListTile(
          leading: Icon(CupertinoIcons.text_badge_plus, color: iconWhiteColor),
          title: Text('Add to playlist ', style: primaryTextStyle20sp),
          onTap: null, //() => _addToPlaylist(),
        ),
        ListTile(
          leading: Icon(
            CupertinoIcons.clock,
            color: iconWhiteColor,
          ),
          title: Text('Delay', style: primaryTextStyle20sp),
          onTap: () {
            DateTime now = DateTime(2021);
            showTimePicker(
                initialEntryMode: TimePickerEntryMode.input,
                context: context,
                initialTime: TimeOfDay(
                  hour: now.hour,
                  minute: now.minute,
                )).then((TimeOfDay? value) {
              if (value != null) {
                Scaffold.of(context).showSnackBar(
                  SnackBar(
                    content: Text(value.format(context)),
                    action: SnackBarAction(
                      label: "Ok",
                      onPressed: () {},
                    ),
                  ),
                );
              }
            });
          },
        ),
        ListTile(
          leading:
              Icon(CupertinoIcons.person_crop_rectangle, color: iconWhiteColor),
          title: Text('See artist', style: primaryTextStyle20sp),
          onTap: null, //() => _openArtistPage(actualMusic),
        ),
        ListTile(
          leading: Icon(CupertinoIcons.stop_circle, color: iconWhiteColor),
          title: Text('Stop', style: primaryTextStyle20sp),
          onTap: () {
            //_player.stop();
            Navigator.pop(context);
          },
        ),
        ListTile(
          leading:
              Icon(CupertinoIcons.slider_horizontal_3, color: iconWhiteColor),
          title: Text('Equalizer', style: primaryTextStyle20sp),
          onTap: _defaultOnTap,
        ),
        ListTile(
          leading: Icon(CupertinoIcons.info_circle_fill, color: iconWhiteColor),
          title: Text('Song Details', style: primaryTextStyle20sp),
          onTap: _defaultOnTap,
        ),
      ];

/*Container _buildMenuBottomSheet(BuildContext context) {
    return Container(
      height: 350,
      padding: EdgeInsets.all(8.0),
      decoration: BoxDecoration(
        color: background.withOpacity(0.9),
        //border: Border.all(color: Colors.blue, width: 2.0),
        /*border: Border(
          top: BorderSide(color: Colors.blue, width: 2.0),
          left: BorderSide(color: Colors.blue, width: 2.0),
          right: BorderSide(color: Colors.blue, width: 2.0),
          bottom: BorderSide(color: Colors.red, width: 2.0),
      ),*/
        //borderRadius: BorderRadius.circular(15.0),
        borderRadius: BorderRadius.only(
            topLeft: Radius.circular(15.0),
            topRight: Radius.circular(15.0)),

      ),
      child: Column(
        children: [
          ListTile(title: Text('Option', style: primaryTextStyle24,),
            trailing: IconButton(
              icon: Icon(Icons.close,color: iconWhiteColor,),
              onPressed: () => Navigator.pop(context),
            ),
          ),
          Expanded(
            child: ListView(
                children: _menuItems()
            ),
          ),
        ],
      ),
    );
  }*/

}


class QueueSongList extends StatelessWidget {
  final SequenceState sequenceState;
  //final ScrollController? scrollController;
  const QueueSongList(SequenceState _sequenceState, {Key? key}) :
        sequenceState = _sequenceState,  super(key: key);

  String fromDuration(Duration duration) {
    return (duration.toString().split('.').first).substring(2);
  }

  @override
  Widget build(BuildContext context) {
    AudioPlayer _player = BlocProvider.of<PlayerController>(context).player;
    bool ignorePointer = false;
    final state = sequenceState;
    final sequence = state.sequence;
    final metadata = state.currentSource!.tag as MediaItem;

    ScrollController scrollController = ScrollController(
        initialScrollOffset: state.currentIndex.toDouble()*73);

    if (sequence.isEmpty) return Container();
    return Expanded(
      //margin:const EdgeInsets.only(top: 32.0),
      //height: MediaQuery.of(context).size.height,
      //padding: const EdgeInsets.all(8.0),

      child: Column(
        children: [
          /*ClipRRect(
            //borderRadius: BorderRadius.circular(15),
            borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(15.0),
                topRight: Radius.circular(15.0)),
            child: AppBar(
              //automaticallyImplyLeading: false,
              backgroundColor: Theme.of(context).primaryColorDark.withOpacity(0.2),
              centerTitle: true,
              leading: IconButton(
                icon: const Icon(CupertinoIcons.chevron_down),
                onPressed: Navigator.of(context).pop,
              ),
              title: const Text("Playlist ● En attente"),
            ),
          ),*/

          /*Container(
            color: Colors.grey,
            height: 1,
          ),*/

          Expanded(
            child: IgnorePointer(
              ignoring: ignorePointer,
              child: ReorderableListView(
                scrollController: ScrollController(
                    initialScrollOffset: state.currentIndex.toDouble()*73),
                onReorder: (int oldIndex, int newIndex) {
                  if (oldIndex < newIndex) newIndex--;
                  //sequence.move(oldIndex, newIndex);

                  //sequence.
                  //sequence.toAlbumModel()
                },
                children: [
                  for (var i = 0; i < sequence.length; i++)
                    Dismissible(
                      key: ValueKey(sequence[i]),
                      background: Container(
                        color: Colors.redAccent,
                        alignment: Alignment.centerRight,
                        child: const Padding(
                          padding: EdgeInsets.only(right: 8.0),
                          child: Icon(Icons.delete, color: Colors.white),
                        ),
                      ),
                      onDismissed: (dismissDirection) {
                        //sequence.removeAt(i);
                      },
                      child: Container(
                        color: i == state.currentIndex
                            ? Theme.of(context).primaryColor.withOpacity(0.5)
                            : null,
                        margin: const EdgeInsets.symmetric(vertical: 0.0),
                        child: Column(
                          children: [
                            Padding(
                              padding:
                              const EdgeInsets.symmetric(horizontal: 4.0),
                              child: Row(
                                children: [
                                  Container(
                                    child: Text('${i + 1}'),
                                  ),
                                  Expanded(
                                    child: ListTile(
                                      onTap: () {
                                        _player.seek(Duration.zero,index: i);
                                      },
                                      //minLeadingWidth: 0.0,
                                      ///horizontalTitleGap: 4.0, // todo: uncomment when ready
                                      ///minVerticalPadding: 0.0, // todo: uncomment when ready
                                      //tileColor: Colors.grey,
                                      leading: ClipRRect(
                                        borderRadius: BorderRadius.circular(5),
                                        child: SizedBox(
                                          height: 40,
                                          width: 40,
                                          child: GetImageCoverItem(
                                            //fit: BoxFit.fitWidth,
                                            futureResource:
                                            OnAudioQuery().queryArtwork(
                                              int.parse(sequence[i].tag.id),
                                              ArtworkType.AUDIO,
                                            ),
                                          ),
                                        ),
                                      ),
                                      title: Text(
                                        sequence[i].tag.title,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                      subtitle: Text(
                                        '${((sequence[i].tag.artist.length <= 10) ? sequence[i].tag.artist : sequence[i].tag.artist.substring(0, 10) + "...") ?? "Unknown Artist"} ● ' +
                                            fromDuration(
                                                (sequence[i].tag as MediaItem)
                                                    .duration ??
                                                    Duration.zero),
                                        //Duration(milliseconds: int.parse(sequence[i].duration!.inMinutes.toString()))
                                        style: const TextStyle(fontSize: 12),
                                      ),
                                      trailing: (i == state.currentIndex)
                                          ? const Icon(Icons.play_arrow) : null,
                                      //trailing: IconButton(icon: Icon(Icons.more_vert), onPressed: () {}),
                                    ),
                                  ),
                                  Container(
                                    child: InkWell(
                                      child: const Icon(Icons.more_vert),
                                      onTap: () {},
                                    ), /*IconButton(
                                              onPressed: (){},
                                              icon: Icon(Icons.more_vert),
                                              ///icon:Icon(Icons.drag_handle_rounded),
                                            ),*/
                                  ),
                                ],
                              ),
                            ),
                            Container(
                              color: Colors.white60,
                              height: 1,
                              margin:
                              const EdgeInsets.symmetric(horizontal: 24),
                            )
                            /*Divider(
                                          color: Colors.white60,
                                          indent: 24.0,
                                          endIndent: 32.0,
                                        ),*/
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
  }
}


extension on List<IndexedAudioSource> {
  bool move(int oldIndex, int newIndex) {
    if(length < newIndex) return false;
    IndexedAudioSource element = elementAt(oldIndex);
    removeAt(oldIndex);
    insert(newIndex-1, element);
    return true;

  }
}


