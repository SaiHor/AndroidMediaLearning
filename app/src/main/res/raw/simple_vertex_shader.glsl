attribute vec4 a_position;
void main(){
    gl_Position = a_position;
    gl_PointSize = 20.0;
}