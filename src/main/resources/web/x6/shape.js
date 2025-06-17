 var start = {
  width: 80,
  height: 36,
  zIndex: 100,
  inherit: 'rect',
  label: "开 始",
  points: [
    [0, 18],
    [20, 0],
    [60, 0],
    [80, 18],
    [60, 36],
    [20, 36],
  ],
  markup: [
      {
        tagName: 'rect',
        selector: 'body',
      },
      {
        tagName: 'text',
        selector: 'label',
      },
    ],
  attrs: {
    body: {
      strokeWidth: 1,
      fill: "#8fcc13",
      // fill: {
      //   type: "linearGradient",
      //   stops: [
      //     { offset: 0, color: "#8fcc13" },
      //     { offset: 1, color: "#e4ffb1" },
      //   ],
      //   attrs: {
      //     StartPoint: [0, 1],
      //     EndPoint: [0, 0],
      //   },
      // },
      stroke: "#7F7F7F",
    },
    label: {
      fill: "#000",
      fontSize: 12,
      textAnchor: "middle",
      textVerticalAnchor: "middle",
      strokeWidth: 0.4,
    },
  },
  data: {
    id: "",
  },
  ports: {
    items: [
      { id: "port_left", group: "groupLeft" },
      { id: "port_bottom", group: "groupBottom" },
      { id: "port_right", group: "groupRight" },
      // { id: "port_top", group: 'groupTop' },
    ],
    groups: {
      groupLeft: {
        position: "left",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
      groupBottom: {
        position: "bottom",

        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
      groupRight: {
        position: "right",

        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
      groupTop: {
        position: "top",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
    },
  },
}

var endNode={
  width: 80,
  height: 36,
  zIndex: 100,
  label: "结 束",
  inherit: 'rect',
  markup: [
      {
        tagName: 'rect',
        selector: 'body',
      },
      {
        tagName: 'text',
        selector: 'label',
      },
    ],
  attrs: {
    body: {
      strokeWidth: 1,
      fill: "#ffffb1",
      // fill: {
      //   type: "linearGradient",
      //   stops: [
      //     { offset: 0, color: "#cccc13" },
      //     { offset: 1, color: "#ffffb1" },
      //   ],
      //   attrs: {
      //     StartPoint: [0, 1],
      //     EndPoint: [0, 0],
      //   },
      // },
      stroke: "#7F7F7F",
      rx: 20,
      ry: 20,
    },
    label: {
      fill: "#000",
      fontSize: 12,
      textAnchor: "middle",
      textVerticalAnchor: "middle",
    },
  },
  data: {
    id: "",
  },
  ports: {
    items: [
      { id: "port_left", group: "groupLeft" },
      // { id: "port_bottom", group: 'groupBottom' },
      { id: "port_right", group: "groupRight" },
      { id: "port_top", group: "groupTop" },
    ],
    groups: {
      groupLeft: {
        position: "left",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
      groupBottom: {
        position: "bottom",

        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
      groupRight: {
        position: "right",

        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
      groupTop: {
        position: "top",

        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
          },
        },

      },
    },
  },
}

var gateway={
  shape:"gateway",
  inherit: 'polygon',
  width: 50,
  height: 50,
  zIndex: 100,  
  label: "网 关",
  points: [
    [0, 20],
    [20, 0],
    [40, 20],
    [20, 40],
  ],
  attrs: {
    body: {
      refPoints: '0,10 10,0 20,10 10,20',
      strokeWidth: 2,
      stroke: '#7F7F7F',
      fill: '#EFF4FF',
    },
    label: {
      text: '+',
      fontSize: 40,
      fill: '#5F95FF',
    },
  },
  
  data: {
    id: "",
  },
  ports: {
    items: [
      { id: "port_left", group: "groupLeft" },
      { id: "port_bottom", group: "groupBottom" },
      { id: "port_right", group: "groupRight" },
      { id: "port_top", group: "groupTop" },
    ],
    groups: {
      groupLeft: {
        position: "left",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
      groupBottom: {
        position: "bottom",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
      groupRight: {
        position: "right",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
      groupTop: {
        position: "top",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
    },
  },
}
const male = 'images/1.png'
var orgNode = {
                width: 180,
                height: 60,
                markup: [{
                        tagName: 'rect',
                        selector: 'body',
                    },
                    {
                        tagName: 'image',
                        selector: 'avatar',
                    },
                    {
                        tagName: 'text',
                        selector: 'rank',
                    },
                    {
                        tagName: 'text',
                        selector: 'name',
                    },
                ],
                attrs: {
                    body: {
                        refWidth: '100%',
                        refHeight: '100%',
                        fill: '#FFFFFF',
                        stroke: '#000000',
                        strokeWidth: 2,
                        rx: 10,
                        ry: 10,
                        pointerEvents: 'visiblePainted',
                    },
                    avatar: {
                        width: 48,
                        height: 48,
                        refX: 8,
                        refY: 6,
                    },
                    rank: {
                        refX: 0.9,
                        refY: 0.2,
                        fontFamily: 'Courier New',
                        fontSize: 14,
                        textAnchor: 'end',
                        //textDecoration: 'underline',
                    },
                    name: {
                        refX: 0.9,
                        refY: 0.6,
                        fontFamily: 'Courier New',
                        fontSize: 14,
                        fontWeight: '800',
                        textAnchor: 'end',
                    },
                },
            }

var normalNode = {
  width: 80,
  height: 34,
  zIndex: 100,
  inherit: 'rect',
  label: "节 点",
  markup: [
      {
        tagName: 'rect',
        selector: 'body',
      },
      {
        tagName: 'text',
        selector: 'label',
      },
    ],
  attrs: {
    body: {
      strokeWidth: 1,
      fill: "#b9cce4",
      stroke: "#7F7F7F",
    },
    label: {
      fill: "#000",
      fontSize: 12,
      textAnchor: "middle",
      textVerticalAnchor: "middle",
    },
    text: {
      textWrap: {
        width: -10,
      },
    },
  },
  data: {
    id: "",
  },
  ports: {
    items: [
      { id: "port_left", group: "groupLeft" },
      { id: "port_bottom", group: "groupBottom" },
      { id: "port_right", group: "groupRight" },
      { id: "port_top", group: "groupTop" },
    ],
    groups: {
      groupLeft: {
        position: "left",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
      groupBottom: {
        position: "bottom",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
      groupRight: {
        position: "right",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
      groupTop: {
        position: "top",
        attrs: {
          circle: {
            r: 4,
            magnet: true,
            stroke: "#5b8ffa",
            strokeWidth: 1,
            fill: "#fff",
            // style: {
            //     visibility: 'hidden',
            // },
          },
        },
      },
    },
  },
}