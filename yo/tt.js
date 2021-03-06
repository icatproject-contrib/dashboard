/**
 * Created by yqa41233 on 20/05/2016.
 */
{
  "site": {
  "topcatUrl": "https://localhost:8181",
    "home" : "my-data",
    "enableEuCookieLaw" : true,
    "paging" : {
    "pagingType": "scroll",
      "paginationNumberOfRows": 10,
      "paginationPageSizes": [
      10,
      25,
      50,
      100,
      1000
    ],
      "scrollPageSize": 50,
      "scrollRowFromEnd": 10
  },
  "breadcrumb": {
    "maxTitleLength": 30
  },
  "serviceStatus": {
    "show": false,
      "message": "<strong>Service status:</strong> Site is down for maintenance"
  },
  "maintenanceMode": {
    "show": false,
      "message": "This server in undergoing maintenance."
  },
  "search": {
    "enableParameters": false,
      "enableSamples": false,
      "gridOptions": {
      "investigation": {
        "columnDefs": [
          {
            "field": "title",
            "link": true
          },
          {
            "field": "visitId",
            "link": true
          },
          {
            "field": "size|bytes"
          },
          {
            "field": "investigationInstrument.fullName"
          },
          {
            "field": "startDate"
          },
          {
            "field": "endDate"
          }
        ]
      },
      "dataset": {
        "enableSelection": true,
          "columnDefs": [
          {
            "field": "name",
            "link": true
          },
          {
            "field": "size|bytes"
          },
          {
            "field": "investigation.title",
            "link": "investigation"
          },
          {
            "field": "createTime"
          },
          {
            "field": "modTime"
          }
        ]
      },
      "datafile": {
        "enableSelection": true,
          "columnDefs": [
          {
            "field": "name"
          },
          {
            "field": "location"
          },
          {
            "field": "fileSize|bytes"
          },
          {
            "field": "dataset.name",
            "link": "dataset"
          },
          {
            "field": "datafileModTime"
          }
        ]
      }
    }
  },
  "browse":{
    "gridOptions": {
      "columnDefs": [
        {
          "field": "fullName",
          "link": true
        },
        {
          "field": "name"
        }
      ]
    },
    "metaTabs": [
      {
        "title": "METATABS.FACILITY.TABTITLE",
        "items": [
          {
            "field": "fullName"
          },
          {
            "field": "description"
          },
          {
            "field": "name"
          },
          {
            "field": "url"
          }
        ]
      }
    ]
  },
  "cart":{
    "gridOptions": {
      "columnDefs": [
        {
          "field": "name"
        },
        {
          "field": "entityType"
        },
        {
          "field": "size"
        },
        {
          "field": "facilityName"
        },
        {
          "field": "status"
        }
      ]
    }
  },
  "myDownloads":{
    "gridOptions": {
      "columnDefs": [
        {
          "field": "fileName"
        },
        {
          "field": "transport"
        },
        {
          "field": "status"
        },
        {
          "field": "createdAt"
        }
      ]
    }
  },
  "pages" : [
    {
      "url" : "/about",
      "stateName": "about",
      "addToNavBar": {
        "linkLabel" : "MAIN_NAVIGATION.ABOUT",
        "align" : "left"
      }

    },
    {
      "url" : "/contact",
      "stateName": "contact",
      "addToNavBar": {
        "linkLabel" : "MAIN_NAVIGATION.CONTACT",
        "align" : "left"
      }

    },
    {
      "url" : "/help",
      "stateName": "help",
      "addToNavBar": {
        "linkLabel" : "MAIN_NAVIGATION.HELP",
        "align" : "left"
      }
    },
    {
      "url" : "/globus-help",
      "stateName": "globus-help"
    },
    {
      "url" : "/cookie-policy",
      "stateName": "cookie-policy"
    }

  ]
},
  {
    "name": "ISIS",
    "title": "ISIS",
    "idsUrl": "https://idsdev.isis.cclrc.ac.uk",
    "hierarchy": [
    "facility",
    "instrument",
    "facilityCycle",
    "investigation",
    "dataset",
    "datafile"
  ],
    "authenticationTypes": [
    {
      "title": "Username/Password",
      "plugin": "uows"
    }

  ],
    "downloadTransportTypes": [
    {
      "type" : "https",
      "idsUrl": "https://idsdev.isis.cclrc.ac.uk"
    }

  ],
    "admin":{
    "gridOptions": {
      "columnDefs": [
        {
          "field": "userName"
        },
        {
          "field": "preparedId"
        },
        {
          "field": "transport"
        },
        {
          "field": "status"
        },
        {
          "field": "size"
        },
        {
          "field": "createdAt"
        },
        {
          "field": "isDeleted"
        }
      ]
    }
  },
    "myData": {
    "entityType" : "investigation",
      "gridOptions": {
      "enableSelection": false,
        "columnDefs": [
        {
          "field": "title",
          "link": true
        },
        {
          "field": "visitId"
        },
        {
          "field": "datafileParameter.numericValue",
          "title": "BROWSE.COLUMN.INVESTIGATION.RUN_NUMBER",
          "where": "datafileParameterType.name = 'run_number'"
        },
        {
          "field": "investigationInstrument.fullName"
        },
        {
          "field": "size"
        },
        {
          "field": "startDate",
          "sort": {
            "direction": "desc",
            "priority": 1
          }
        },
        {
          "field": "endDate"
        }
      ]
    }
  },
    "browse":{
    "instrument": {
      "gridOptions": {
        "columnDefs": [
          {
            "field": "fullName",
            "link": true
          }
        ]
      },
      "metaTabs": [
        {
          "title": "METATABS.INSTRUMENT.TABTITLE",
          "items": [
            {
              "field": "fullName"
            },
            {
              "field": "description"
            },
            {
              "label": "METATABS.INSTRUMENT.TYPE",
              "field": "type"
            },
            {
              "label": "METATABS.INSTRUMENT.URL",
              "field": "url"
            }
          ]
        },
        {
          "title": "METATABS.INSTRUMENT_SCIENTISTS.TABTITLE",
          "items": [
            {
              "field": "instrumentScientist.fullName"
            }
          ]
        }
      ]
    },
    "investigation": {
      "gridOptions": {
        "enableSelection": true,
          "columnDefs": [
          {
            "field": "title",
            "sort": {
              "direction": "asc"
            },
            "link": true
          },
          {
            "field": "visitId",
            "link": true
          },
          {
            "field": "name",
            "link": true
          },
          {
            "field": "size|bytes"
          },
          {
            "field": "investigationInstrument.fullName"
          },
          {
            "field": "startDate",
            "sort": {
              "direction": "desc",
              "priority": 1
            }
          },
          {
            "field": "endDate"
          }
        ]
      },
      "metaTabs": [
        {
          "title": "METATABS.INVESTIGATION.TABTITLE",
          "items": [
            {
              "field": "name"
            },
            {
              "field": "title"
            },
            {
              "field": "summary"
            },
            {
              "field": "startDate",
              "template": "{{item.value | date:'yyyy-MM-dd'}}"
            },
            {
              "field": "endDate",
              "template": "{{item.value | date:'yyyy-MM-dd'}}"
            }
          ]
        },
        {
          "title": "METATABS.INVESTIGATION_USERS.TABTITLE",
          "items": [
            {
              "field": "investigationUser.fullName"
            }

          ]
        },
        {
          "title": "METATABS.INVESTIGATION_SAMPLES.TABTITLE",
          "items": [
            {
              "field": "investigationSample.name"
            }
          ]
        },
        {
          "title": "Publications",
          "items": [
            {
              "field": "publication.fullReference"
            }
          ]
        }
      ]
    },
    "dataset": {
      "gridOptions": {
        "enableSelection": true,
          "columnDefs": [
          {
            "field": "name",
            "link": true
          },
          {
            "field": "size|bytes"
          },
          {
            "field": "createTime",
            "sort": {
              "direction": "desc",
              "priority": 1
            }
          },
          {
            "field": "modTime"
          }
        ]
      },
      "metaTabs": [
        {
          "title": "METATABS.DATASET.TABTITLE",
          "items": [
            {
              "field": "name"
            },
            {
              "field": "description"
            },
            {
              "field": "startDate",
              "template": "{{item.value | date:'yyyy-MM-dd'}}"
            },
            {
              "field": "endDate",
              "template": "{{item.value | date:'yyyy-MM-dd'}}"
            }
          ]
        },
        {
          "title": "METATABS.DATASET_TYPE.TABTITLE",
          "items": [
            {
              "field": "datasetType.name"
            },
            {
              "field": "datasetType.description"
            }
          ]
        }
      ]
    },
    "facilityCycle": {
      "gridOptions": {
        "columnDefs": [
          {
            "field": "name",
            "sort": {
              "direction": "asc"
            },
            "link": true
          },
          {
            "field": "description"
          },
          {
            "field": "startDate",
            "sort": {
              "direction": "desc",
              "priority": 1
            }
          },
          {
            "field": "endDate"
          }
        ]
      }
    },
    "datafile": {
      "gridOptions": {
        "enableSelection": true,
          "enableDownload": true,
          "columnDefs": [
          {
            "field": "name"
          },
          {
            "field": "location"
          },
          {
            "field": "fileSize|bytes"
          },
          {
            "field": "datafileModTime",
            "sort": {
              "direction": "desc",
              "priority": 1
            }
          }
        ]
      },
      "metaTabs": [
        {
          "title": "METATABS.DATAFILE.TABTITLE",
          "items": [
            {
              "field": "name"
            },
            {
              "field": "description"
            },
            {
              "field": "fileSize",
              "template": "{{item.value | bytes}}"
            },
            {
              "field": "location"
            }
          ]
        },
        {
          "title": "METATABS.DATAFILE.PARAMETERS",
          "items": [
            {
              "label": "",
              "field": "datafileParameter[entity.type.valueType=='STRING'].stringValue",
              "template": "<span class='label'>{{item.entity.type.name}}</span><span class='value'>{{item.value}}</span>"
            },
            {
              "label": "",
              "field": "datafileParameter[entity.type.valueType=='NUMERIC'].numericValue",
              "template": "<span class='label'>{{item.entity.type.name}}</span><span class='value'>{{item.value}}</span>"
            },
            {
              "label": "",
              "field": "datafileParameter[entity.type.valueType=='DATE_AND_TIME'].datetimeValue",
              "template": "<span class='label'>{{item.entity.type.name}}</span><span class='value'>{{item.value | date:'yyyy-MM-dd'}}</span>"
            }
          ]
        }
      ]
    }
  }
  }

}
