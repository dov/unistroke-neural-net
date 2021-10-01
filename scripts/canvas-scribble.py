#!/usr/bin/python
######################################################################
#  An example of how to do single stroke scribbe with the goo
#  canvas. This can be used to accumulate data and testing
#  a neural network for unistroke recognition.
#
#  Dov Grobgeld <dov.grobgeld@gmail.com>
#  2020-03-15 Sun
######################################################################

import gi
gi.require_version('GooCanvas', '2.0')
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk, GooCanvas
import pdb


class MyWindow(Gtk.Window):
    def __init__(self):
        Gtk.Window.__init__(self, title="Unistroke scribble")
        self.set_default_size (640, 600);
        vbox = Gtk.Box(spacing=6,orientation=Gtk.Orientation.VERTICAL)
        self.add(vbox)
        self.cnv = GooCanvas.Canvas()
        vbox.pack_start(self.cnv,True,True,0)

        root = self.cnv.get_root_item()
        root.connect('button-press-event', self.on_root_clicked)
        root.connect('button-release-event', self.on_root_released)
        root.connect('motion-notify-event', self.on_root_motion)
        self.in_stroke = False
        points = GooCanvas.CanvasPoints.new(1)
        self.poly = GooCanvas.CanvasPolyline(
          parent = root,
          points = points,
          line_width=8.0,
          stroke_color = 'green',
          fill_color = None,
          close_path=False,
          line_cap = 1,  # Round
          line_join = 1, 
          )

        self.button1 = Gtk.Button(label='Quit')
        self.button1.connect("clicked", Gtk.main_quit)
        vbox.pack_start(self.button1, False, False, 0)
        self.path = None


    def on_root_clicked(self,item,target_item,event):
        self.in_stroke = True
        self.path = [(event.x,event.y)]

    def on_root_released(self,item,target_item,event):
        self.in_stroke = False

    def on_root_motion(self,item,target_item,event):
        if self.in_stroke:
          self.path += [(event.x,event.y)]
  
          points = GooCanvas.CanvasPoints.new(len(self.path))
          for i,pp in enumerate(self.path):
            points.set_point(idx=i, x=pp[0], y=pp[1])
          
          self.poly.set_property('points', points)
        
win = MyWindow()
win.connect("destroy", Gtk.main_quit)
win.show_all()
Gtk.main()
