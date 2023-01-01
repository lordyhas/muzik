
// Defines the content of the search page in `showSearch()`.
// SearchDelegate has a member `query` which is the query string.
import 'dart:async';

import 'package:flutter/material.dart';
import 'package:muzik_audio_player/src/music_player_page.dart';
import 'package:muzik_audio_player/src/widget_model/widgets.dart';
import 'package:on_audio_query/on_audio_query.dart';


class ResearchDelegate extends SearchDelegate<SongModel?>{
  final List<SongModel> songs;
  ResearchDelegate(this.songs) : super(
    searchFieldStyle: const TextStyle(color: Colors.black38),
  );
  @override
  List<Widget> buildActions(BuildContext context) {
    return <Widget>[
      IconButton(
        icon: const Icon(Icons.clear),
        onPressed: (){
            query = "";
        },
      ),
    ];
  }

  @override
  ThemeData appBarTheme(BuildContext context) {
    //super.appBarTheme(context);
    return Theme.of(context);
  }

  @override
  Widget buildLeading(BuildContext context) {
    return IconButton(
        onPressed: (){
          close(context, null);
        },
        icon: const Icon(Icons.arrow_back));
  }

  @override
  Widget buildResults(BuildContext context) {
    return Container();
  }

  @override
  Widget buildSuggestions(BuildContext context) {
    final Iterable<SongModel> suggestions = songs.where((prod) {
      if(prod.title.toLowerCase().contains(query.toLowerCase())) return true;

      if(prod.album!.toLowerCase().contains(query.toLowerCase())) return true;
      if(prod.artist!.toLowerCase().contains(query.toLowerCase())) return true;


      return false;
    });
    return ListView(
      children: suggestions.map((e) => Text(e.title)).toList(),
    );
  }

}

class MusicSearchDelegate extends SearchDelegate<SongModel?> {
  final List<SongModel> _words;
  final List<SongModel> _history;

  MusicSearchDelegate(List<SongModel> words, {history = const <SongModel>[]})
      : _words = words,
        _history = history,
        super(searchFieldStyle: const TextStyle(color: Colors.black38),);


  SongModel? _productFind;


  // Leading icon in search bar.
  @override
  Widget buildLeading(BuildContext context) {
    ///appBarTheme();
    return IconButton(
      tooltip: 'Back',
      icon: AnimatedIcon(
        icon: AnimatedIcons.menu_arrow,
        progress: transitionAnimation,
      ),
      onPressed: () {
        //SearchDelegate.close() can return values, similar to Navigator.pop().
        close(context, null);
        ///this.close(context, null);
      },
    );
  }

  // Widget of result page.
  @override
  Widget buildResults(BuildContext context) {

    SongModel product = _words.where((element) => element.title==query).first;
    return const Padding(
      padding: EdgeInsets.all(8.0),);
  }

  // Suggestions list while typing (this.query).
  @override
  Widget buildSuggestions(BuildContext context) {
    final Iterable<SongModel> suggestions = query.isEmpty
        ? _history
        : _words.where((prod) {
          if(prod.title.toLowerCase().contains(query.toLowerCase())) return true;
          if(prod.album!.toLowerCase().contains(query.toLowerCase())) return true;
          if(prod.artist!.toLowerCase().contains(query.toLowerCase())) return true;
          return false;
    });

    return BooleanBuilder(
      check: _words.isNotEmpty ,
      ifFalse: Container(
        alignment: Alignment.center,
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          crossAxisAlignment: CrossAxisAlignment.center,
          children: const [
            Icon(Icons.wifi_off_outlined, color: Colors.black, size: 102,),
            Text('No Internet'),
          ],
        ),
      ),
      ifTrue: _SuggestionList(
        query: query,
        suggestions: suggestions.toList(),
        onSelected: (SongModel suggestion) {
          query = suggestion.title;
          _productFind = suggestion;
          if(_history.contains(suggestion)) {
            _history.remove(suggestion);
          }
          _history.insert(0, suggestion);
          showResults(context);
          //this.close(context, this._productFind!);
        },
      ),
    );
  }

  // Action buttons at the right of search bar.
  @override
  List<Widget> buildActions(BuildContext context) {
    return <Widget>[
      if (query.isEmpty)
        IconButton(
          tooltip: 'Voice Search',
          icon: const Icon(Icons.mic),
          onPressed: () {
            query = 'voice input coming soon';
          },
        )
      else
        IconButton(
          tooltip: 'Clear',
          icon: const Icon(Icons.clear),
          onPressed: () {
            query = '';
            showSuggestions(context);
          },
        )
    ];
  }

  @override
  ThemeData appBarTheme(BuildContext context) {
    //super.appBarTheme(context);
    return Theme.of(context);
    /*return ThemeData(
      primaryColor: Colors.deepPurpleAccent,
      primaryIconTheme: IconThemeData(
        color: Colors.white,
      ),
      inputDecorationTheme: InputDecorationTheme(
        hintStyle:
        Theme.of(context).textTheme.title.copyWith(color: Colors.white),
      ),
    );*/
  }

  @override
  void showResults(BuildContext context) {
    super.showResults(context);
    //if(!this._history.contains(this._productFind) && this.query.isNotEmpty)
      //this._history.insert(0, this._productFind);

  }
}

// Suggestions list widget displayed in the search page.
class _SuggestionList extends StatelessWidget {
  const _SuggestionList({this.suggestions, this.query, this.onSelected});

  final List<SongModel>? suggestions;
  final String? query;
  final ValueChanged<SongModel>? onSelected;

  @override
  Widget build(BuildContext context) {
    final textTheme = Theme.of(context).textTheme.subtitle1;
    return ListView.builder(
      itemCount: suggestions!.length,
      itemBuilder: (BuildContext context, int i) {
        final SongModel suggestion = suggestions![i];
        return query!.isEmpty
            ? ListTile(
                leading: const Icon(Icons.history) ,
                // Highlight the substring that matched the query.
                title: RichText(
                  text: TextSpan(
                    text: suggestion.title,
                    style: textTheme!.copyWith(fontWeight: FontWeight.bold),
                    children: const <TextSpan>[
                      /*TextSpan(
                        text: suggestion.substring(query!.length),
                        style: textTheme,
                      ),*/
                    ],
                  ),
                ),
                onTap: () {
                  onSelected!(suggestion);
                },
              )
            : Card(
                child: InkWell(
                  onTap: () {
                    onSelected!(suggestion);
                  },
                  child: Container(
                    margin: const EdgeInsets.symmetric(vertical: 8.0),
                    child: Row(
                      children: [
                        Container(
                          margin: const EdgeInsets.symmetric(horizontal: 8.0),
                          height: 80,
                          width: 80,
                          decoration: BoxDecoration(
                            border: Border.all(
                              //width: 2.0,
                              color:Theme.of(context).primaryColorLight,
                            ),
                            borderRadius: BorderRadius.circular(5),
                            /*image: DecorationImage(
                              image: MemoryImage(suggestion.artUri!.bytes),
                              //AssetImage('assets/img/p1/product_${i+ 1}.png'),
                            ),*/

                          ),

                          child: ClipRRect(
                            borderRadius: BorderRadius.circular(5),
                            child: GetImageCoverItem(
                              //fit: BoxFit.fitWidth,
                              futureResource: OnAudioQuery().queryArtwork(
                                suggestion.id,
                                ArtworkType.AUDIO,
                              ),
                            ),
                          ),
                        ),
                        Expanded(
                          child: SizedBox(
                            height: 80,
                            child: ListTile(
                              title: RichText(
                                text: TextSpan(
                                  text: suggestion.title,
                                  style: textTheme!.copyWith(fontWeight: FontWeight.bold),
                                  children: <TextSpan>[
                                    TextSpan(
                                      text: "\n${suggestion.album}",
                                      style: textTheme,
                                    ),
                                    TextSpan(
                                      text: "\n${suggestion.artist}",
                                      style: textTheme,
                                    ),
                                    /*TextSpan(
                                      text: "\n${suggestion.} en Stock",
                                      style: textTheme,
                                    ),*/
                                  ],
                                ),
                              ),
                            )
                          ),
                        ),
                      ],
                    ),
              ),
                ),
            );
      },
    );
  }
}