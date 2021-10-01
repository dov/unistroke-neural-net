#!/usr/bin/python
# -*- Encoding: utf-8 -*-
######################################################################
#  An application for viewing and annotating the gesture paths
#  recorded by the DovUniStroke.
#
#  Dov Grobgeld <dov.grobgeld@gmail.com>
#  2020-03-15 Sun
######################################################################

import gi
gi.require_version('GooCanvas', '2.0')
gi.require_version('Gtk', '3.0')
from gi.repository import Gtk, GooCanvas, Gdk, Pango
import pdb
import glob,os,json,shutil


class MyWindow(Gtk.Window):
    def __init__(self, glyphdir):
        Gtk.Window.__init__(self, title="View glyph files")
        self.glyphdir = glyphdir
        self.glyphfiles = sorted(glob.glob(os.path.join(glyphdir, '*.json')))
        self.current_glyph_index = 0
        self.set_focus()
        self.connect('key-press-event',
                     self.on_key_press)

        self.set_default_size (1200, 1200);
        vbox = Gtk.Box(spacing=6,orientation=Gtk.Orientation.VERTICAL)
        self.add(vbox)

        hpaned = Gtk.Paned(orientation=Gtk.Orientation.VERTICAL)
        hpaned.set_position(600)
        vbox.pack_start(hpaned, True, True, 0)

        self.cnv = GooCanvas.Canvas()
        hpaned.add1(self.cnv)

        sw = Gtk.ScrolledWindow()
        hpaned.add2(sw)

        self.textview = Gtk.TextView()
        self.textbuffer = self.textview.get_buffer()
        sw.add(self.textview)

        # Create some tags for the text view
        self.tag_bold = self.textbuffer.create_tag('fat',
                                                   weight=Pango.Weight.BOLD)

        hbox = Gtk.ButtonBox()
        hbox.set_layout(Gtk.ButtonBoxStyle.END)

        button_prev = Gtk.Button(label='←')
        button_prev.connect("clicked", self.prev_glyph)
        hbox.pack_start(button_prev, True, True, 0)

        button_next = Gtk.Button(label='→')
        button_next.connect("clicked", self.next_glyph)
        hbox.pack_start(button_next, True, True, 0)

        button_quit = Gtk.Button(label='Quit')
        button_quit.connect("clicked", Gtk.main_quit)
        hbox.pack_start(button_quit, True, True, 0)

        vbox.pack_start(hbox, False, False, 0)

        self.path = None
        self.show_current_glyph()


    def create_points(self,points):
        cp = GooCanvas.CanvasPoints.new(len(points))
        for i,p in enumerate(points):
            cp.set_point(i,p[0],p[1])
        return cp
        
    def show_current_glyph(self):
        glyph_filename = self.glyphfiles[self.current_glyph_index]
        jj = json.load(open(glyph_filename))

        root = self.cnv.get_root_item()
        if self.path is not None:
            self.path.remove()

        self.path = GooCanvas.CanvasGroup(parent=root)
        points = [(v['x'],v['y']) for v in jj['gesture']]
                      
        GooCanvas.CanvasPolyline(
          parent = self.path,
          points = self.create_points(points),
          line_width=2.0,
          stroke_color = 'green',
          close_path=False)
        
        GooCanvas.CanvasEllipse(
            parent = self.path,
            center_x = points[0][0],
            center_y = points[0][1],
            radius_x = 10,
            radius_y = 10,
            stroke_color='black',
            line_width=2,
            fill_color='brown')

        text = ('<b>'+os.path.basename(glyph_filename) + '</b>\n\n'
               + 'Predictions\n')
        for i,pp in enumerate(jj['predictions']):
            text += '  ' + pp['name'] + (': %.02f'%pp['score'])+'\n'
            if i==5:
                break
        text += 'Modifier: ' + str(jj.get('mModifier','??'))+'\n'
        text += 'GestureSet: ' + str(jj.get('mGestureSet','??'))+'\n'
        if 'ground_truth' in jj:
            text += '<b>Ground truth: '+jj['ground_truth']+'</b>\n'
        self.textbuffer.set_text('')
        start_iter = self.textbuffer.get_start_iter()
        self.textbuffer.insert_markup(start_iter, text, -1)

    def next_glyph(self,button=None):
        self.current_glyph_index += 1
        if self.current_glyph_index >= len(self.glyphfiles):
            self.current_glyph_index = 0
        self.show_current_glyph()
    
    def prev_glyph(self,button=None):
        self.current_glyph_index -= 1
        if self.current_glyph_index < 0:
            self.current_glyph_index = len(self.glyphfiles)-1
        self.show_current_glyph()

    def update_glyph_name(self, glyph_name):
        with open(self.glyphfiles[self.current_glyph_index]) as fp:
            obj = json.load(fp)
            obj['ground_truth'] = glyph_name
        shutil.copy(self.glyphfiles[self.current_glyph_index],
                    self.glyphfiles[self.current_glyph_index]+'.bak')
        with open(self.glyphfiles[self.current_glyph_index],'w') as fp:
            json.dump(obj, fp, indent=2)
        self.show_current_glyph()

    def on_marker_clicked(self,item,target_item,event,user_data):
        print('i=', user_data)

    def on_marker_enter(self,item,target_item,event):
        item.set_property('fill_color','orange')

    def on_marker_leave(self,item,target_item,event):
        item.set_property('fill_color','brown')

    def on_key_press(self, widget, event):
        keyval_name = Gdk.keyval_name(event.keyval)
        if keyval_name == 'Left':
            self.prev_glyph()
        elif keyval_name == 'Right':
            self.next_glyph()
        elif keyval_name == 'g':
            self.glyph_name = None
            self.dialog = Gtk.Dialog('GetKeyname',self)
            entry_name = Gtk.Entry()
            entry_name.connect("key-press-event", self.on_get_key_name_key_press, self)
            
            self.dialog.get_content_area().pack_start(entry_name, False, False, 10)
            entry_name.show()
            self.dialog.add_button('cancel', Gtk.ResponseType.CANCEL)
            self.dialog.add_button('OK', Gtk.ResponseType.ACCEPT)
            res = self.dialog.run()
            if self.glyph_name is not None or res == Gtk.ResponseType.ACCEPT:
                if self.glyph_name is None:
                    self.glyph_name = entry_name.get_text()
                self.update_glyph_name(self.glyph_name)
                print('Ok', self.glyph_name)
            else:
                print('Cancel')
            self.dialog.destroy()
        return True

    
    def on_get_key_name_key_press(self, widget, event, undef):
        keyval_name = Gdk.keyval_name(event.keyval)
        if keyval_name == 'Return':
            self.glyph_name = widget.get_text()
            self.dialog.destroy()
        return 0

            
if __name__ == '__main__':
    import sys

    argp = 1
    glyphdir = sys.argv[argp]

    win = MyWindow(glyphdir)
    win.connect("destroy", Gtk.main_quit)
    win.show_all()
    Gtk.main()
