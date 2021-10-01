# This script renders the output from the dov unistroke android
# input method to mnist like images.

#!/usr/bin/python

######################################################################
#  Convert accumulated on the phones to mnist like 32 by 32 images.
#
#  Dov Grobgeld <dov.grobgeld@gmail.com>
#  2021-09-27 Mon
######################################################################

from PIL import Image
from aggdraw import Draw,Brush,Path as DrawPath,Pen
from npeuclid import Vec2Array
import sys
from pathlib import Path 
import json
import numpy as np
import pdb

argp = 1
glyphdir = Path(sys.argv[argp] if argp < len(sys.argv) else '/tmp/dov-uni-stroke')

outdir = glyphdir # Meanwhile put the outputs in the same place

margin = 4
r = 2.5

cw=ch = 32-2*margin  # Active canvas araa
for i,fn in enumerate(glyphdir.glob('*.json')):
    with open(fn) as fh:
      jj = json.load(fh)
    points = Vec2Array([(v['x'],v['y']) for v in jj['gesture']])
    minx,miny = np.array(points[:,0]).min(),np.array(points[:,1]).min()
    maxx,maxy = np.array(points[:,0]).max(),np.array(points[:,1]).max()
    x0,y0 = margin,margin
    sx = cw/(maxx-minx)
    sy = ch/(maxy-miny)
    if sx < sy:
      sy = sx
      y0 = margin + (ch-sy*(maxy-miny))/2
    elif sy < sx:
      sx = sy
      x0 = margin + (cw-sx*(maxx-minx))/2
    
    im = Image.new("L", (32, 32))
    draw = Draw(im)
    brush = Brush('#000')
    
    draw.rectangle((0, 0, im.size[0], im.size[1]), brush)
    
    pen = Pen((255,) * 3, 2)
    brush = Brush('#fff')
    p = DrawPath()
    for j,xy in enumerate(points):
        x = x0 + sx * (xy[0]-minx)
        y = y0 + sy * (xy[1]-miny)
        if j==0:
            p.moveto(x,y)
            # place a circle at the first point
            draw.ellipse((x-r,y-r,x+r,y+r), brush)
        else:
            p.lineto(x,y)
    
    draw.line(p, pen)
    
    draw.flush()
    im.save(fn.with_suffix('.png'))
print('ok')

