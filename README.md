# MCWiki

"Putting the gamepedia into the game"

This plugin aims to present information that can only be accessed through the minecraft wiki (externally) in game (internally).


## Features

Displays information from the minecraft wiki in player chat or book<br>
Configurable amount of lines in chat<br>
Multi language support<br>
Full json rich text interface<br>
Inline links to article sections, other articles, or other sites<br>
Table of contents support<br>


## Commands

**/wiki [article]:** displays content of article.<br>
**/mcwiki:** displays meta information.<br>
**/mcwiki reload:** reloads config.<br>

## Config

bookmode: defaults to true. If false, displays in chat instead of book.<br>
language: source language to retrieve articles from. eg: 'es' or 'de'<br>
cutoff: amount of p elements to display. Default: 5.<br>
customsite: url of an unofficial custom wiki-like page to display. <br>
format: customize the display formatting of elements using json if so desired.

## Acknowledgements 

This project uses jedk1 and DarkBlade12's utility classes 

[SpigotMc](https://www.spigotmc.org/resources/mcwiki.35039)<br>
[Github](https://github.com/skylerdev/mcwiki)<br>
