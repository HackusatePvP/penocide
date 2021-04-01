# Penocide
Penocide is a Staff plugin that runs entirely on events. Please note this was made for testing purposes only and is not optimized to run on actual networks.

## How it works
All staff commands/modules run on events. There are a few reasons why it works like this. 
1. Message configuration
2. Flexible API

Since everything runs on events you can cancel staff events and commands from processing. In this plugin we have implemented a simple combat check to display a possible 
staff abuse check. When in combat (located in StaffListener) the StaffModuleEnterEvent will be cancelled. 

## Warning
This was a just a theory I was testing. Most of the events are redundant and should be modified before put into actual use. The only reason why everything runs on events it
to fully display the possibilities the project has. I also do not know much about optimizations, so I'm not quite sure how "optimal" running commands on events would be. 
Most of this project is untested and may have some errors. I will not be maintaining this project, and I may never update the project to fix any discovered errors. 
