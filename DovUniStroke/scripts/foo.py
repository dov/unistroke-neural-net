#!/usr/bin/python

######################################################################
#  Create mnist like bitmaps.
#
#  Dov Grobgeld <dov.grobgeld@gmail.com>
#  2021-09-27 Mon
######################################################################

from PIL import Image,ImageDraw
import sys

argp = 1
glyphdir = sys.argv[argp] if argp < len(sys.argv) else '/tmp/foo'

outdir = glyphdir # Meanwhile put the outputs in the same place

im = Image.new("1", (32, 32))
draw = ImageDraw.Draw(im)

draw.rectangle((0, 0, im.size[0], im.size[1]), fill='#000')
draw.line(((4, 4), (24,20)), fill='#fff',width=3)

im.save('/tmp/foo.png')
print('ok')

