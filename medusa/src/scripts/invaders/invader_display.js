parent.fill(255);
parent.stroke(255);
parent.beginShape();
parent.vertex(x, y + height);
parent.vertex(x + (width / 3), y);
parent.vertex(x + (width / 3) * 2, y);
parent.vertex(x + width, y + height);
parent.vertex(x, y + height);
parent.endShape();