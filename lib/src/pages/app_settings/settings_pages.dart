import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';


import 'About.dart';


class TextTheme{
  final ThemeData? theme;
  final String? text;
  final bool? selected;

  TextTheme({this.theme, this.text, this.selected});
}

//import 'package:flutter/cupertino.dart';

class SettingsWidget extends StatefulWidget {
  const SettingsWidget({Key? key, this.title}) : super(key: key);
  final String? title;

  static Route route() => MaterialPageRoute(
      builder: (BuildContext context) => const SettingsWidget());

  @override
  _SettingsState createState() => _SettingsState();
}

class _SettingsState extends State<SettingsWidget> {

  final TextStyle primaryTextStyle20 =  const TextStyle(color: Colors.white, fontSize: 20);
  var timeUpdate =  DateTime.now();
  var textWhiteColor = Colors.white;

  String version ="0.4.1";

  bool _switchVal = true;
  bool _checkVal  = true;
  bool _isGrid = true;
  bool _autoPlay = false;

  void _defaultOnTap(){

    ScaffoldMessenger.of(context)
      ..hideCurrentSnackBar()
      ..showSnackBar(const SnackBar(
          behavior: SnackBarBehavior.floating,
          content: Text('En développment | Soon :) ')
      ));

  }

  TextStyle textSettingsStyle() => TextStyle(color: Colors.blue[600]);
  @override
  Widget build(BuildContext context) {

    double sliderVal = 50.0;
    double indent = 42;

    Future<void> showTimer() => showTimePicker(
      context: context,
      confirmText: "CHANGE",
      initialTime: const TimeOfDay(hour: 0, minute: 0,),
      initialEntryMode: TimePickerEntryMode.input,
    ).then((TimeOfDay? value){
      if(value != null){
        Duration duration = Duration(hours: value.hour, minutes: value.minute);
        Scaffold.of(context).showSnackBar(
          SnackBar(
            content: Text(value.format(context)),
            action: SnackBarAction(label: "UNDO",onPressed: showTimer,),
          ),
        );
      }
    });

    return Scaffold(
      //backgroundColor: background,
      appBar: AppBar(
        centerTitle: true,
        elevation: 5.0,
        //backgroundColor: background,
        title: Text(widget.title ?? "Settings"),
        //leading: Icon(Icons.home),

      ),
      body: SingleChildScrollView(
        child: Container(
          padding: const EdgeInsets.only(left: 10, bottom: 20),
          child: Column(
            //mainAxisAlignment: MainAxisAlignment.start,
            children: <Widget>[
              const SizedBox(height: 10,),

              ListTile(title: Text("Préférence :",style: textSettingsStyle())),
              ListTile(
                title: Text("Mise en veille",style: primaryTextStyle20,),
                subtitle: Text("desative",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
                onTap: (){
                  DateTime now = DateTime.now();
                  Duration duration = Duration();
                  showTimer();

                },
              ),
              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              ListTile(
                title: Text("Fondu de lecture",style: primaryTextStyle20,),
                subtitle: Text("Controlez comment la lecture doit changer le morceaux",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
                onTap: _defaultOnTap,
              ),

              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              ListTile(
                title: Text("Vitesse de lecture",style: primaryTextStyle20,),
                subtitle: Slider(
                    activeColor: Theme.of(context).primaryColor,
                    min: 0.0,
                    max: 100.0,
                    divisions: 4,
                    label: "${sliderVal.round()/50}",
                    onChanged: (double value){
                      setState(() {
                        sliderVal = value;
                      });
                    },
                    value: sliderVal,

                ),
              ),
              //Divider(indent: 15, endIndent: 15, color: Colors.white30,),
              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              CheckboxListTile(
                activeColor: Theme.of(context).primaryColorDark,
                value: this._checkVal,
                onChanged: (bool? value){
                  ///setState(() {this._checkVal = value;});
                },
                title: Text("Notification",style: primaryTextStyle20,),
                subtitle: Text("Toutes notifications seront ${(this._checkVal)?"desativées": "activées"}",
                  style: TextStyle(color: Theme.of(context).primaryColor),),

              ),
              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              CheckboxListTile(
                activeColor: Theme.of(context).primaryColorDark,
                value: this._autoPlay,
                onChanged: (bool? value){
                  ///setState(() {this._autoPlay = value;});
                },
                title: Text("Reprendre la lecture",style: primaryTextStyle20,),
                subtitle: Text("Reprendre la lorsque le casque est branché",
                  style: TextStyle(color: Theme.of(context).primaryColor),
                ),

              ),

              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              ListTile(
                title: Text("Gestion de la bibliothèque",style: primaryTextStyle20,),
                subtitle: Text("Personnalisez l'ordre des listes",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
              ),


              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              CheckboxListTile(
                activeColor: Theme.of(context).primaryColorDark,
                value: _isGrid,
                onChanged: (bool? value){
                  ///setState(() {this._isGrid = value;});
                },
                title: Text("Grid Album",style: primaryTextStyle20,),
                subtitle: Text('Afficher Artistes et Albums en ${(this._isGrid)?"Grille": "Liste"}',
                  style: TextStyle(color: Theme.of(context).primaryColor),
                ),

              ),

              Divider(indent: indent, endIndent: indent, color: Colors.white30,),

              ListTile(
                title: Text("Changer le theme",style: primaryTextStyle20,),
                subtitle: Text("Personnalisez la couleur ",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
                onTap: (){
                  /*showDialog(
                    context: context,
                    builder: (_){

                      List<String> titleTheme = [
                        "Theme Violet",
                        "Theme Blue",
                        "Theme Orange",
                        "Theme Special",
                      ];
                      List<TextTheme> options = List<TextTheme>.generate(titleTheme.length,
                          (i)=> TextTheme(
                              theme: Styles.listTheme[i],
                              text:  titleTheme[i],)
                      );
                      return AlertDialog(
                        content: Container(
                          child: BlocBuilder<Styles,ThemeData>(
                            builder: (ctx,theme){
                              return PlaylistDialogContent(
                                options: options.map((option) => option.text).toList(),
                                initialIndexSelected: options.indexWhere((element) {
                                  return (element.theme.primaryColor == theme.primaryColor)
                                      ? true
                                      : false;
                                }),
                                onSelected: (index){
                                  //ctx.bloc<Styles>().
                                  BlocProvider.of<Styles>(context).switchThemeTo(ThemeModeState.values[index]);
                                  Navigator.pop(context);
                                },
                              );
                            },
                          ),
                        ),
                      );
                    },
                  );*/
                },
              ),


              //Divider(indent: 15, endIndent: 15, color: Colors.white30,),
              /// ListTile OF DARK THEME
              /*ListTile(
                title: Text("Changer le theme",style: primaryTextStyle20,),
                subtitle: Text("dark mode",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
                trailing: Switch(
                    onChanged:  (bool value){
                      setState(() {this._switchVal = value; });
                    },
                    value: this._switchVal,
                ),
              ),*/

              SizedBox(height: 10,),
              Divider(indent: 8, endIndent: 8, color: Colors.white30,),


              ListTile(title: Text("Cloud Option :",style: textSettingsStyle()),),

              ListTile(
                title: Text("Connexion Mode",style: primaryTextStyle20,),
                subtitle: Text("Choisir votre cloud music",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
              ),
              Divider(indent: indent, endIndent: indent, color: Colors.white30,),
              ListTile(
                title: Text("Sync Mode",style: primaryTextStyle20,),
                subtitle: Text("Synchroniser au cloud music",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
              ),

              const Divider(indent: 8, endIndent: 8, color: Colors.white30,),


              const SizedBox(height: 10,),
              ListTile(title: Text("À propos de l'appli :",style: textSettingsStyle()),),
              ListTile(
                leading: Icon(Icons.copyright, color: Colors.white,),
                title: Text("Version",style:primaryTextStyle20,),
                subtitle: Text(version),
                onTap: (){

                },
              ),
              ListTile(
                leading: const Icon(Icons.star_half, color: Colors.white,),
                title: Text("Évaluer l'application", style: primaryTextStyle20,),
                subtitle: const Text("Évaluer cette application sur Play Store"),
                onTap: (){
                },
              ),
              ListTile(
                leading: const Icon(Icons.new_releases, color: Colors.white,),
                title: Text("A propos & Licences",style:primaryTextStyle20,),
                subtitle: const Text("Biblithèques tierces"),
                onTap: (){
                  Navigator.push(context,  MaterialPageRoute(
                      builder: (BuildContext context) =>  const AboutWidget()
                  ));
                },
              ),

          /* ListTile(
                title: Text("Developer",style: primaryTextStyle20,),
                subtitle: Text("Hassan Kajila",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
              ),

              Divider(indent: 15, endIndent: 15, color: Colors.white30,),
              ListTile(
                title: Text("App Version",style: primaryTextStyle20,),
                subtitle: Text("$version",
                  style: TextStyle(color: Theme.of(context).primaryColor),),
              ),

              Divider(indent: 15, endIndent: 15, color: Colors.white30,),
              */

            ],
          ),
        ),
      ),
    );
  }
}