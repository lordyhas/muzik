import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_bloc/flutter_bloc.dart';
import 'package:muzik_audio_player/data/values.dart';
import 'package:muzik_audio_player/src/pages/app_settings/About.dart';

import 'package:muzik_audio_player/src/widget_model/widgets.dart';

class TextThemeOption {
  final String text;
  final bool? selected;
  final int index;

  TextThemeOption({required this.text, required this.index, this.selected});
}

//import 'package:flutter/cupertino.dart';

class SettingPage extends StatefulWidget {
  const SettingPage({Key? key, this.title}) : super(key: key);
  final String? title;

  static Route route() => MaterialPageRoute(
      builder: (BuildContext context) => const SettingPage());

  @override
  _SettingsState createState() => _SettingsState();
}

class _SettingsState extends State<SettingPage> {
  final TextStyle primaryTextStyle20 =
      const TextStyle(color: Colors.white,);
  var timeUpdate = DateTime.now();
  var textWhiteColor = Colors.white;

  String version = "0.4.1";

  final bool _checkVal = true;
  final bool _isGrid = true;
  final bool _autoPlay = false;

  //late SettingState settingState;

  @override
  void initState() {
    super.initState();
    //settingState = BlocProvider.of<SettingCubit>(context).state;

  }

  void _defaultOnTap() {
    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(const SnackBar(
          behavior: SnackBarBehavior.floating,
          content: Text('En développment | Soon :) ')));
  }



  TextStyle textSettingsStyle() =>
      TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.blue[600]);

  @override
  Widget build(BuildContext context) {
    double sliderVal = 50.0;

    Future<void> showTimer() => showTimePicker(
          context: context,
          confirmText: "CHANGE",
          initialTime: const TimeOfDay(
            hour: 0,
            minute: 0,
          ),
          initialEntryMode: TimePickerEntryMode.input,
        ).then((TimeOfDay? value) {
          if (value != null) {
            //Duration duration = Duration(hours: value.hour, minutes: value.minute);
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text(value.format(context)),
                action: SnackBarAction(
                  label: "UNDO",
                  onPressed: showTimer,
                ),
              ),
            );
          }
        });

    return Scaffold(
      //backgroundColor: Theme.of(context).cardColor,
      appBar: AppBar(
        centerTitle: true,
        //elevation: 5.0,
        //backgroundColor: background,
        title: Text(widget.title ?? "Settings"),
        //leading: Icon(Icons.home),
      ),
      body: SingleChildScrollView(
        child: Container(
          //padding: const EdgeInsets.only(left: 10, bottom: 20),
          child: BlocBuilder<SettingCubit, SettingState>(
            builder: (context, setting) {
              return Column(
                //mainAxisAlignment: MainAxisAlignment.start,
                children: <Widget>[
                  Card(
                    child: Column(
                      children: [
                        ListTile(title: Text("Préférence", style: textSettingsStyle())),
                        ListTile(
                          title: Text(
                            "Mise en veille",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "desative",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                          onTap: () {
                            //DateTime now = DateTime.now();
                            //Duration duration = const Duration();
                            showTimer();
                          },
                        ),
                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),
                        ListTile(
                          title: Text(
                            "Vitesse de lecture",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Slider(
                            activeColor: Theme.of(context).primaryColor,
                            min: 0.0,
                            max: 100.0,
                            divisions: 4,
                            label: "${sliderVal.round() / 50}",
                            onChanged: (double value) => BlocProvider
                                .of<SettingCubit>(context)
                                .save(setting..speed=value),

                            value: setting.speed,
                          ),
                        ),

                        ListTile(
                          title: Text(
                            "Fondu de lecture",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Controlez comment la lecture doit changer le morceaux",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                          onTap: _defaultOnTap,
                        ),

                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),


                        //Divider(indent: 15, endIndent: 15, color: Colors.white30,),
                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),

                        CheckboxListTile(
                          activeColor: Theme.of(context).primaryColorDark,
                          value: _checkVal,
                          onChanged: (bool? value) {
                            ///setState(() {this._checkVal = value;});
                          },
                          title: Text(
                            "Notification",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Toutes notifications seront ${(_checkVal) ? "desativées" : "activées"}",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                        ),
                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),

                        CheckboxListTile(
                          activeColor: Theme.of(context).primaryColorDark,
                          value: _autoPlay,
                          onChanged: (bool? value) {
                            ///setState(() {this._autoPlay = value;});
                          },
                          title: Text(
                            "Reprendre la lecture",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Reprendre la lorsque le casque est branché",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                        ),

                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),

                        ListTile(
                          title: Text(
                            "Gestion de la bibliothèque",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Personnalisez l'ordre des listes",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                        ),

                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),

                        CheckboxListTile(
                          activeColor: Theme.of(context).primaryColorDark,
                          value: setting.displayAsGrid,
                          onChanged: (value) => BlocProvider.of<SettingCubit>(context).save(setting..displayAsGrid = value!),
                          title: Text(
                            "Grid Album",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            'Afficher Artistes et Albums en ${(_isGrid) ? "Grille" : "Liste"}',
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                        ),

                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),

                        ListTile(
                          title: Text(
                            "Changer le theme",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Personnalisez la couleur ",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                          onTap: () {
                            showDialog(
                              context: context,
                              builder: (_) {
                                List<TextThemeOption> options = [
                                  TextThemeOption(
                                    text: "Theme Classic",
                                    index: ThemeModeState.classic.index,
                                  ),
                                  TextThemeOption(
                                      text: "Theme Blue",
                                      index: ThemeModeState.cyan.index),
                                  TextThemeOption(
                                      text: "Theme Orange",
                                      index: ThemeModeState.orange.index),
                                  TextThemeOption(
                                      text: "Theme Special",
                                      index: ThemeModeState.special.index),
                                  TextThemeOption(
                                      text: "Theme Cool",
                                      index: ThemeModeState.cool.index),
                                  TextThemeOption(
                                      text: "Theme Random",
                                      index: ThemeModeState.random.index),
                                ];

                                return AlertDialog(
                                  content: BlocBuilder<StyleBloc, StyleState>(
                                    builder: (ctx, style) {
                                      return PlaylistDialogContent(
                                        options: options,
                                        initialIndexSelected: options.indexWhere(
                                                (element) => element
                                                .index == style.themeMode.index),
                                        onSelected: (index) {
                                          //ctx.bloc<Styles>().
                                          BlocProvider
                                              .of<StyleBloc>(context)
                                              .switchThemeTo(ThemeModeState
                                              .values[index]);
                                          Navigator.of(context).pop();

                                          SystemChrome.setSystemUIOverlayStyle(
                                              SystemUiOverlayStyle(
                                                systemNavigationBarDividerColor: BlocProvider
                                                    .of<StyleBloc>(context).state.theme.primaryColor,
                                          ));
                                          /*Navigator.of(context).pushReplacement(
                                              SettingPage.route()
                                          );*/
                                        },
                                      );
                                    },
                                  ),
                                );
                              },
                            );
                          },
                        ),
                      ],
                    ),
                  ),

                  const SizedBox(
                    height: 10,
                  ),


                  ///----------------------------------------------------------

                  Card(
                    child: Column(
                      children: [
                        ListTile(
                          title: Text("Cloud Option", style: textSettingsStyle()),
                        ),

                        ListTile(
                          title: Text(
                            "Connexion Mode",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Choisir votre cloud music",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                        ),
                        //Divider(indent: indent, endIndent: indent, color: Colors.white30,),
                        ListTile(
                          title: Text(
                            "Sync Mode",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(
                            "Synchroniser au cloud music",
                            style: TextStyle(color: Theme.of(context).primaryColor),
                          ),
                        ),
                      ],
                    ),
                  ),


                  const SizedBox(
                    height: 10,
                  ),

                  ///----------------------------------------------------------

                  Card(
                    child: Column(
                      children: [
                        ListTile(
                          title:
                          Text("À propos de l'appli", style: textSettingsStyle()),
                        ),
                        ListTile(
                          leading: const Icon(
                            Icons.copyright,
                            color: Colors.white,
                          ),
                          title: Text(
                            "Version",
                            style: primaryTextStyle20,
                          ),
                          subtitle: Text(version),
                          onTap: () {},
                        ),
                        ListTile(
                          leading: const Icon(
                            Icons.star_half,
                            color: Colors.white,
                          ),
                          title: Text(
                            "Évaluer l'application",
                            style: primaryTextStyle20,
                          ),
                          subtitle:
                          const Text("Évaluer cette application sur Play Store"),
                          onTap: () {},
                        ),
                        ListTile(
                          leading: const Icon(
                            Icons.new_releases,
                            color: Colors.white,
                          ),
                          title: Text(
                            "A propos & Licences",
                            style: primaryTextStyle20,
                          ),
                          subtitle: const Text("Biblithèques tierces"),
                          onTap: () {
                            Navigator.push(
                                context,
                                MaterialPageRoute(
                                    builder: (BuildContext context) =>
                                    const AboutWidget()));
                          },
                        ),
                      ],
                    ),
                  ),

                ],
              );
            },
          )
        ),
      ),
    );
  }
}
