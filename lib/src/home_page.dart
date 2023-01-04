import 'dart:io';

import 'package:animated_text_kit/animated_text_kit.dart';
import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:get/get_core/src/get_main.dart';
import 'package:get/get_navigation/src/extension_navigation.dart';
import 'package:just_audio/just_audio.dart';
import 'package:muzik_audio_player/data/app_bloc/music_player_bloc/player_controller_cubit.dart';
import 'package:muzik_audio_player/data/audio_repository/audio_song_info.dart';
import 'package:muzik_audio_player/src/pages/app_settings/about.dart';
import 'package:muzik_audio_player/src/pages/app_settings/settings_pages.dart';
import 'package:muzik_audio_player/src/search/search_activity.dart';
import 'package:muzik_audio_player/src/widget_model/mini_player.dart';
import 'package:on_audio_query/on_audio_query.dart';
import 'package:muzik_audio_player/src/music_player_page.dart';
import 'package:muzik_audio_player/src/layout/music_layout.dart';
import 'package:muzik_audio_player/res.dart';

part 'elevated_drawer.dart';

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  _HomePageState createState() => _HomePageState();
}

class _HomePageState extends State<HomePage>
    with SingleTickerProviderStateMixin {
  bool isDrawerOpen = false;

  final Duration duration = const Duration(milliseconds: 500);

  final GlobalKey<ScaffoldState> _scaffoldKey = GlobalKey<ScaffoldState>();

  //late AnimationController _animationController;
  //OnAudioQuery audioQuery = OnAudioQuery();
  late PlayerControllerBloc musicController;
  //= BlocProvider.of<PlayerController>(context);

  @override
  void initState() {
    super.initState();
    musicController = BlocProvider.of<PlayerControllerBloc>(context);
    setMusic();


  }
  //PlayerController get state =>  BlocProvider.of<PlayerController>(context);
  void _onShowBottomPlayer(){

  }

  void setMusic() async {
    //var playlist = await ObjectBoxManager().getActualPlaylist();
    //musicController.restoreListSong();

  }

  void closeDrawer(){
    setState(() {
      isDrawerOpen = false;
    });
  }


  @override
  Widget build(BuildContext context) {
    SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle(
      statusBarColor: Colors.transparent,
      statusBarIconBrightness: Brightness.light,
      statusBarBrightness: Brightness.dark,
      systemNavigationBarColor: Theme.of(context).scaffoldBackgroundColor,
      systemNavigationBarDividerColor:Theme.of(context).primaryColor,
      systemNavigationBarIconBrightness: Brightness.light,
    ));


    return DefaultTabController(
      length: 3,
      child: Scaffold(
        key: _scaffoldKey,
        //extendBodyBehindAppBar: true,
        extendBody: true,
        bottomNavigationBar: const BottomAppBar(
          color: Colors.transparent,
          child: Padding(
            padding: EdgeInsets.only(bottom: 1.0),
            child: BottomPlayerView(),
          ),
        ),
        body: StreamBuilder<List<SongModel>>(
          stream: OnAudioQuery()
              .querySongs(
            sortType: SongSortType.TITLE,
            orderType: OrderType.ASC_OR_SMALLER,
            uriType: UriType.EXTERNAL,
          ).asStream(),
          builder: (context, searchSnapshot) {
            return Scaffold(

              //backgroundColor: const Color(0xffF8F9FB),
              extendBodyBehindAppBar: true,
              appBar: AppBar(
                //brightness: Brightness.light,
                backgroundColor: Theme.of(context)
                    .appBarTheme
                    .backgroundColor!
                    .withOpacity(0.90),
                elevation: 5,
                leading: IconButton(
                  color: isDrawerOpen ? Colors.transparent : null,
                  icon: const Icon(CupertinoIcons.bars),
                  onPressed: () {
                    setState(() {
                      isDrawerOpen = true;
                    });
                    showDialog(
                        context: context,
                        builder: (_) {
                          return Container(
                            color: Colors.grey.shade700.withOpacity(0.8),
                            child: Column(
                              children: [
                                AppBar(
                                  backgroundColor: Colors.transparent,
                                  elevation: 0,
                                  centerTitle: true,
                                  leading: IconButton(
                                    icon: const Icon(CupertinoIcons.xmark),
                                    onPressed: (){
                                      closeDrawer();
                                      Get.back();
                                    },
                                  ),
                                  title: const Text('MyZIK'),
                                ),
                                Expanded(
                                  child: ElevatedDrawer(
                                    color: Colors.transparent,
                                    onTapItem: (){
                                      closeDrawer();
                                      Navigator.of(context).pop();
                                    },
                                  ),
                                ),
                                Material(
                                  color: Colors.transparent,
                                  child: Row(
                                    children: [
                                      const Spacer(),
                                      Ink(
                                        decoration: const BoxDecoration(
                                            color: Colors.grey,
                                            shape: BoxShape.circle),
                                        child: IconButton(
                                          //color: Colors.black.withOpacity(.7),
                                          tooltip: 'Settings',
                                          enableFeedback: true,
                                          icon: const Icon(
                                            CupertinoIcons.gear_alt_fill,
                                            //color: Colors.black.withOpacity(.7),
                                          ),
                                          onPressed: () {
                                            //HapticFeedback.lightImpact();
                                            Navigator.push(
                                                context, SettingPage.route());
                                            // Navigator.push(context,
                                            //MaterialPageRoute(builder: (context) => settingsPage()));
                                          },
                                        ),
                                      ),
                                      const SizedBox(
                                        width: 4.0,
                                      ),
                                      Ink(
                                        decoration: const BoxDecoration(
                                            color: Colors.grey,
                                            shape: BoxShape.circle),
                                        child: IconButton(
                                          //color: Colors.black.withOpacity(.7),
                                          tooltip: 'Settings',
                                          enableFeedback: true,
                                          icon: const Icon(
                                            CupertinoIcons.info,
                                            //color: Colors.black.withOpacity(.7),
                                          ),
                                          onPressed: () {
                                            //HapticFeedback.lightImpact();
                                            Navigator.push(
                                                context, AboutWidget.route());
                                            // Navigator.push(context,
                                            //     MaterialPageRoute(builder: (context) => settingsPage()));
                                          },
                                        ),
                                      ),
                                      const SizedBox(
                                        width: 4.0,
                                      ),
                                      Ink(
                                        decoration: const BoxDecoration(
                                            color: Colors.grey,
                                            shape: BoxShape.circle),
                                        child: IconButton(
                                            onPressed: Navigator.of(context).pop,
                                            icon: const Icon(
                                                CupertinoIcons.square_arrow_right)),
                                      ),
                                      const SizedBox(
                                        width: 8.0,
                                      )
                                    ],
                                  ),
                                ),
                                const SizedBox(
                                  height: 8.0,
                                ),
                              ],
                            ),
                          );
                        });
                  },

                ),
                //shadowColor: Color(0xffF0F0F0).withOpacity(.4),
                //backgroundColor: Colors.white,
                title: AnimatedTextKit(
                  animatedTexts: [
                    TypewriterAnimatedText(
                      'Music P',
                      speed: const Duration(milliseconds: 150),
                      curve: Curves.bounceIn,
                      textStyle: const TextStyle(
                          //fontSize: 30,
                          //fontWeight: FontWeight.bold,
                          ),
                    ),
                    //TyperAnimatedText(text)
                    TyperAnimatedText(
                      'MyZIK Player',
                      speed: const Duration(milliseconds: 150),
                      curve: Curves.easeInCirc,
                      textStyle: const TextStyle(
                          //fontSize: 30,
                          //fontWeight: FontWeight.bold,
                          ),
                    ),
                  ],
                  //isRepeatingAnimation: true,
                  //repeatForever: true,
                  displayFullTextOnTap: true,
                  stopPauseOnTap: false,
                  totalRepeatCount: 1,
                ),
                actions: [
                  IconButton(
                    //color: Colors.black.withOpacity(.7),
                    tooltip: 'Search',
                    enableFeedback: true,
                    icon: const Icon(
                      CupertinoIcons.search,
                      //color: Colors.black.withOpacity(.7),
                    ),
                    onPressed: () {
                      //_showBottomPlayer();
                      HapticFeedback.lightImpact();
                      showSearch<SongModel?>(
                          context: context,
                          delegate: //ProductSearchDelegate(list),
                          MusicSearchDelegate(
                            !searchSnapshot.hasData
                                ? <SongModel>[ ]
                                : <SongModel>[...searchSnapshot.data! ],
                            history: !searchSnapshot.hasData
                                ? <SongModel>[ ]
                                : <SongModel>[...searchSnapshot.data!.getRange(0, 3).toList() ],
                          )
                      );
                    },
                  ),
                  /*StreamBuilder<SequenceState?>(
                      stream: musicController.player.sequenceStateStream,
                      builder: (context, snapshot) {
                        if(snapshot.data?.sequence.isEmpty ?? true) {
                          return const SizedBox();
                        }
                        return  IconButton(
                          onPressed: () => Navigator.push(
                              context, MusicPlayerPage.route(),
                          ),
                          icon: const Icon(CupertinoIcons.arrowtriangle_right_square),
                        );
                      }),*/

                ],
                bottom: TabBar(
                  indicatorColor: Theme.of(context).primaryColor.withOpacity(.8),
                  labelColor: Theme.of(context).primaryColor,
                  //unselectedLabelStyle:
                  //TextStyle(color: Colors.black.withOpacity(.5)),
                  //unselectedLabelColor: Colors.black.withOpacity(.4),
                  indicatorSize: TabBarIndicatorSize.label,
                  //labelColor: Colors.black.withOpacity(.8),
                  tabs: const [
                    Tab(
                      child: Text(
                        'SONGS',
                      ),
                    ),
                    Tab(
                      child: Text(
                        'ALBUM',
                      ),
                    ),
                    Tab(
                      child: Text(
                        'ARTIST',
                      ),
                    ),
                  ],
                ),
              ),
              body:  Stack(
                children: [
                  TabBarView(
                    children: [
                      SongListScreen(onTapCallBack: _onShowBottomPlayer,),
                      AlbumListScreen(onTapCallBack: _onShowBottomPlayer,),
                      ArtistListScreen(onTapCallBack: _onShowBottomPlayer,),
                    ],
                  ),
                   /*const Align(
                    alignment: Alignment.bottomCenter,
                    child: BottomPlayerView(),
                   ),*/
                ],
              ),
            );
          }
        ),
      ),
    );
  }
}


/// this method return a bottom (display) player.


/*
Future<void> musicStreamingDialog() async {
  double indent = 34.0;
  return await showDialog<void>(
      context: context,
      builder: (BuildContext context) {
        return SimpleDialog(
          title: Text("Music Streaming Platform"),
          children: <Widget>[

            SimpleDialogOption(
              child:
              OutlineButton(
                  borderSide: const BorderSide(
                    width: 2.0,
                    color: Colors.deepOrange,
                  ),
                  //padding: EdgeInsets.all(8.0),
                  onPressed: (){
                    Navigator.pop(context);
                    //_defaultOnTap();
                  },
                  child: const ListTile(
                    leading: Icon(FontAwesomeIcons.google, color: Colors.deepOrange,),
                    title: Text("Play Music",
                        style: TextStyle(fontSize: 18,color: Colors.deepOrange, fontWeight: FontWeight.bold )
                    ),
                  )
              ),
            ),
            Divider(indent: indent, endIndent: indent,),
            SimpleDialogOption(
              child: OutlineButton(
                borderSide: const BorderSide(
                  width: 2.0,
                  color: Colors.purpleAccent,
                ),
                onPressed: (){
                  Navigator.pop(context);
                  _defaultOnTap();
                },
                child: ListTile(
                  //leading: Icon(FontAwesomeIcons.itunes, color: Colors.purpleAccent),
                  leading: SizedBox(
                      height: 25,
                      child: Image.asset("assets/icons/itune.png")
                  ),
                  title: const Text("iTunes",
                    style: TextStyle(
                        fontSize: 18,
                        color: Colors.purpleAccent,
                        fontWeight: FontWeight.bold
                    ),
                  ),
                ),
              ),
            ),
            Divider(indent: indent, endIndent: indent,),
            SimpleDialogOption(
              child: OutlineButton(
                textColor: Colors.green,
                borderSide: BorderSide(
                  width: 2.0,
                  color: Colors.green,
                ),
                onPressed: (){
                  Navigator.pop(context);
                  _defaultOnTap();
                },
                child: ListTile(
                  leading: Icon(FontAwesomeIcons.spotify, color: Colors.green),
                  title: Text("Spotify",
                    style: TextStyle(
                        fontSize: 18,
                        color: Colors.green,
                        fontWeight: FontWeight.bold
                    ),
                  ),
                ),
              ),
            ),
            Divider(indent: indent, endIndent: indent,),
            SimpleDialogOption(
              child: OutlineButton(
                //textColor: Colors.black,
                borderSide: BorderSide(
                  width: 2.0,
                  color: Colors.black,
                ),
                onPressed: (){
                  Navigator.pop(context);
                  _defaultOnTap();
                },
                child: ListTile(
                  leading: Container(
                      height: 25,
                      child: Image.asset("assets/icons/deezer.png")
                  ),
                  title: Text("Deezer",
                    style: TextStyle(
                        fontSize: 18,
                        color: Colors.black,
                        fontWeight: FontWeight.bold
                    ),
                  ),
                ),
              ),
            ),
            /*SimpleDialogOption(
                onPressed: () {
                  //writeMail(strings['about_unh_mail']);
                  Navigator.pop(context);
                },
                child: ListTile(
                  leading: Icon(Icons.mail),
                  title: Text("{strings['about_unh_mail']}"),
                ),
              ),*/
          ],
        );
      }
  );

}

*/