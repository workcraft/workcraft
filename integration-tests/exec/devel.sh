SPECIAL_LINE=" == expected js line"

./workcraft -nogui -exec:<(echo "
    print('$SPECIAL_LINE');
    exit();
") \
| grep -q "$SPECIAL_LINE" || error "Workcraft (dev) did not start up correctly"
