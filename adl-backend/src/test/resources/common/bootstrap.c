void __component_global_init(void);
void __component_global_shutdown(void);

int main(int argc, char *argv[]){
  int r;
  __component_global_init();
  r = CALL(entryPoint, main)(argc, argv);
  __component_global_shutdown();
  return r;
}
