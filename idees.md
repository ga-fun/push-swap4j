# Quelques idées :

## Pas certains que le sb dans la phase 1 soit très rentable avec une phase B un peu optimisée.

A vérifier

## On peut faire des pa-ra des valeurs qu'on croise dans B et qui sont supérieures au dernier élément de A.

Dans le cas où on trouve la cible - 1, il faut pas faire de ra, il sera moins cher de faire un sa (voire un ss) après avoir poussé la cible.  
Attention, dans ce cas particulier, il faut se méfier de cas limites (à définir).

Par exemple si on cherche 494 avec 480 en fin de pile A, et qu'en chemin, on passe sur 486, on peut le coller en fin de pile A. Voire reporter le ra à plus tard pour pouvoir insérer d'autres éléments.

Ce n'est peut-être pas toujours une bonne idée. Un cas où ça marche très bien, c'est quand il y a un élément compris entre l'élément courant et la cible qui se trouve du côté d'où on vient.  
Exemple : 491 ,..., 486, ..., 494
Dans ce cas, la manip pa-ra évite de repasser sur 486 en sens inverse pour atteindre 491, une fois 494 poussé. Ca n'est pas encore obligé d'entre meilleur, mais on a des chances : 
Avec le pa-ra : 1 pa, 1 ra, puis un rra pour le ramener en haut de pile (3 ops). 
Sans le pa-ra : 1 rb (ou rrb) de plus pour le sauter au retour, d'autres mouvements éventuels quand l'élément devient la cible, puis un pa (>= 2 ops).

Je crois que dès qu'on arrive à en pousser 2, on est gagnant.
