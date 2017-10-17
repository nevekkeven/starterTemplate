# OpenCV pour la detection d'objets et la labelisation de couleurs d'interets:

* Translation du programme `python` pour la detection d'objet en temps reel en code java en utilisant le tutoriel
de https://gurus.pyimagesearch.com/object-tracking-in-video/

* J'ai tout mis dans `onCameraFrame`, c'est un peu long , je ne sais pas si quelqu'un a ume meilleure idee

* L'application marche mais on n'est pas encore rendu au niveau du tutoriel (image noir et blanc dans notre cas)


L'idee du code est de segmenter l'image de la camera pour detecter des objets (`GaussianBlur`,`erode`,`dilate`), de detecter
les contours, d'entourer les contours d'un certain niveau  rayon puis de labeliser si ce contour est bleu